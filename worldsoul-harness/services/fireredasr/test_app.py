"""HTTP and WAV validation tests for the FireRedASR adapter."""

from __future__ import annotations

import io
import wave
from pathlib import Path

from fastapi.testclient import TestClient

from app import MODEL_NAME, create_app


class FakeTranscriber:
    device = "test"

    def transcribe(self, wav_path: Path) -> str:
        assert wav_path.is_file()
        return "向前走十格"


def wav_bytes(*, channels: int = 1, sample_width: int = 2, rate: int = 16_000) -> bytes:
    output = io.BytesIO()
    with wave.open(output, "wb") as audio:
        audio.setnchannels(channels)
        audio.setsampwidth(sample_width)
        audio.setframerate(rate)
        audio.writeframes(b"\x00\x00" * 4_000 * channels)
    return output.getvalue()


def test_transcribes_supported_pcm_wav() -> None:
    with TestClient(create_app(FakeTranscriber())) as client:
        response = client.post(
            "/v1/audio/transcriptions",
            data={"model": MODEL_NAME},
            files={"file": ("voice.wav", wav_bytes(), "audio/wav")},
        )
        assert response.status_code == 200
        assert response.json() == {"text": "向前走十格"}


def test_rejects_incompatible_sample_rate_before_inference() -> None:
    with TestClient(create_app(FakeTranscriber())) as client:
        response = client.post(
            "/v1/audio/transcriptions",
            data={"model": MODEL_NAME},
            files={"file": ("voice.wav", wav_bytes(rate=8_000), "audio/wav")},
        )
        assert response.status_code == 415
        assert response.json()["detail"] == "WAV audio must use a 16000 Hz sample rate"


def test_rejects_unknown_model() -> None:
    with TestClient(create_app(FakeTranscriber())) as client:
        response = client.post(
            "/v1/audio/transcriptions",
            data={"model": "unknown"},
            files={"file": ("voice.wav", wav_bytes(), "audio/wav")},
        )
        assert response.status_code == 400
