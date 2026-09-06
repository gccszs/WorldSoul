"""Loopback OpenAI-compatible transcription service backed by FireRedASR-AED-L."""

from __future__ import annotations

import asyncio
import os
import sys
import tempfile
import wave
from contextlib import asynccontextmanager
from pathlib import Path
from typing import AsyncIterator, Protocol
from uuid import uuid4

from fastapi import FastAPI, File, Form, HTTPException, UploadFile


MODEL_NAME = "FireRedASR-AED-L"
MAX_AUDIO_BYTES = int(os.environ.get("WORLDSOUL_FIRERED_MAX_AUDIO_BYTES", "4194304"))


class Transcriber(Protocol):
    """Minimal transcription interface used by the HTTP adapter."""

    def transcribe(self, wav_path: Path) -> str:
        """Return text for one validated WAV file."""


class FireRedTranscriber:
    """Single-GPU FireRedASR-AED-L runtime."""

    def __init__(self, source_dir: Path, model_dir: Path, device: str) -> None:
        if not source_dir.is_dir():
            raise RuntimeError(f"FireRedASR source directory does not exist: {source_dir}")
        required = ("model.pth.tar", "cmvn.ark", "dict.txt", "train_bpe1000.model")
        missing = [name for name in required if not (model_dir / name).is_file()]
        if missing:
            raise RuntimeError(f"FireRedASR model is incomplete: {', '.join(missing)}")
        sys.path.insert(0, str(source_dir))
        import torch
        from fireredasr.models.fireredasr import FireRedAsr

        if device == "auto":
            device = "cuda" if torch.cuda.is_available() else "cpu"
        if device not in ("cuda", "cpu"):
            raise RuntimeError("WORLDSOUL_FIRERED_DEVICE must be auto, cuda, or cpu")
        if device == "cuda" and not torch.cuda.is_available():
            raise RuntimeError("CUDA was requested but is unavailable to PyTorch")
        self._use_gpu = device == "cuda"
        self.device = device
        self._model = FireRedAsr.from_pretrained("aed", str(model_dir))

    def transcribe(self, wav_path: Path) -> str:
        results = self._model.transcribe(
            [uuid4().hex],
            [str(wav_path)],
            {
                "use_gpu": 1 if self._use_gpu else 0,
                "beam_size": 3,
                "nbest": 1,
                "decode_max_len": 0,
                "softmax_smoothing": 1.25,
                "aed_length_penalty": 0.6,
                "eos_penalty": 1.0,
            },
        )
        if not results or not isinstance(results[0].get("text"), str):
            raise RuntimeError("FireRedASR returned no transcript")
        return results[0]["text"].strip()


def _validate_wav(path: Path) -> float:
    try:
        with wave.open(str(path), "rb") as audio:
            if audio.getnchannels() != 1:
                raise HTTPException(415, "WAV audio must be mono")
            if audio.getsampwidth() != 2:
                raise HTTPException(415, "WAV audio must use 16-bit PCM samples")
            if audio.getframerate() != 16_000:
                raise HTTPException(415, "WAV audio must use a 16000 Hz sample rate")
            frames = audio.getnframes()
            if frames <= 0:
                raise HTTPException(400, "WAV audio is empty")
            duration = frames / audio.getframerate()
            if duration > 60:
                raise HTTPException(413, "WAV audio exceeds the 60 second model limit")
            return duration
    except HTTPException:
        raise
    except (EOFError, wave.Error) as error:
        raise HTTPException(415, "audio is not a valid PCM WAV file") from error


def _load_runtime() -> FireRedTranscriber:
    runtime_root = Path(os.environ.get("WORLDSOUL_FIRERED_RUNTIME", Path(__file__).parents[2] / ".runtime" / "fireredasr"))
    source_dir = Path(os.environ.get("WORLDSOUL_FIRERED_SOURCE", runtime_root / "source"))
    model_dir = Path(os.environ.get("WORLDSOUL_FIRERED_MODEL", runtime_root / "model"))
    device = os.environ.get("WORLDSOUL_FIRERED_DEVICE", "auto").lower()
    return FireRedTranscriber(source_dir, model_dir, device)


def create_app(runtime: Transcriber | None = None) -> FastAPI:
    """Create a service application, optionally with a test transcriber."""

    state: dict[str, Transcriber] = {}
    inference_lock = asyncio.Lock()

    @asynccontextmanager
    async def lifespan(_: FastAPI) -> AsyncIterator[None]:
        state["runtime"] = runtime if runtime is not None else await asyncio.to_thread(_load_runtime)
        yield

    service = FastAPI(title="WorldSoul FireRedASR", version="1.0", lifespan=lifespan)

    @service.get("/health")
    async def health() -> dict[str, object]:
        active = state.get("runtime")
        return {
            "status": "ready" if active is not None else "starting",
            "model": MODEL_NAME,
            "device": getattr(active, "device", "test"),
        }

    @service.post("/v1/audio/transcriptions")
    async def transcription(
        file: UploadFile = File(...),
        model: str = Form(MODEL_NAME),
    ) -> dict[str, str]:
        if model.lower() not in (MODEL_NAME.lower(), "fireredasr-aed-l", "aed"):
            raise HTTPException(400, f"unsupported model: {model}")
        content = await file.read(MAX_AUDIO_BYTES + 1)
        if len(content) > MAX_AUDIO_BYTES:
            raise HTTPException(413, "audio exceeds the configured byte limit")
        with tempfile.TemporaryDirectory(prefix="worldsoul-fireredasr-") as temporary:
            wav_path = Path(temporary) / "voice.wav"
            wav_path.write_bytes(content)
            _validate_wav(wav_path)
            active = state.get("runtime")
            if active is None:
                raise HTTPException(503, "speech model is still loading")
            try:
                async with inference_lock:
                    text = await asyncio.to_thread(active.transcribe, wav_path)
            except HTTPException:
                raise
            except Exception as error:
                raise HTTPException(500, "speech recognition failed") from error
        if not text:
            raise HTTPException(422, "speech recognition returned no text")
        return {"text": text}

    return service


app = create_app()
