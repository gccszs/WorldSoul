param(
    [string]$SourceRevision = '834635e4cf277ed8ca92049fc375b17c3dc20748',
    [string]$ModelRepository = 'FireRedTeam/FireRedASR-AED-L'
)

$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path -Parent $PSScriptRoot
$harnessRoot = Join-Path $repositoryRoot 'worldsoul-harness'
$runtimeRoot = Join-Path $harnessRoot '.runtime\fireredasr'
$sourceRoot = Join-Path $runtimeRoot 'source'
$modelRoot = Join-Path $runtimeRoot 'model'
$environmentRoot = Join-Path $runtimeRoot 'env'
$serviceRequirements = Join-Path $harnessRoot 'services\fireredasr\requirements.txt'
$condaCommand = Get-Command conda -ErrorAction Stop
$gitCommand = Get-Command git -ErrorAction Stop

New-Item -ItemType Directory -Force -Path $runtimeRoot | Out-Null
if (-not (Test-Path -LiteralPath (Join-Path $sourceRoot '.git'))) {
    if (Test-Path -LiteralPath $sourceRoot) {
        throw "FireRedASR source target exists but is not a Git checkout: $sourceRoot"
    }
    & $gitCommand.Source clone https://github.com/FireRedTeam/FireRedASR.git $sourceRoot
    if ($LASTEXITCODE -ne 0) { throw "FireRedASR clone failed with exit code $LASTEXITCODE" }
}

Push-Location $sourceRoot
try {
    $changes = & $gitCommand.Source status --porcelain
    if ($changes) { throw "FireRedASR runtime source has local changes; refusing to overwrite them." }
    & $gitCommand.Source fetch origin $SourceRevision
    if ($LASTEXITCODE -ne 0) { throw "FireRedASR fetch failed with exit code $LASTEXITCODE" }
    & $gitCommand.Source checkout --detach $SourceRevision
    if ($LASTEXITCODE -ne 0) { throw "FireRedASR checkout failed with exit code $LASTEXITCODE" }
} finally {
    Pop-Location
}

if (-not (Test-Path -LiteralPath (Join-Path $environmentRoot 'python.exe'))) {
    & $condaCommand.Source create --prefix $environmentRoot python=3.10 pip -y
    if ($LASTEXITCODE -ne 0) { throw "Conda environment creation failed with exit code $LASTEXITCODE" }
}

& $condaCommand.Source run --prefix $environmentRoot python -m pip install --upgrade pip
if ($LASTEXITCODE -ne 0) { throw "pip upgrade failed with exit code $LASTEXITCODE" }
& $condaCommand.Source run --prefix $environmentRoot python -m pip install torch==2.5.1 --index-url https://download.pytorch.org/whl/cu124
if ($LASTEXITCODE -ne 0) { throw "CUDA PyTorch installation failed with exit code $LASTEXITCODE" }
& $condaCommand.Source run --prefix $environmentRoot python -m pip install -r (Join-Path $sourceRoot 'requirements.txt') -r $serviceRequirements
if ($LASTEXITCODE -ne 0) { throw "FireRedASR dependency installation failed with exit code $LASTEXITCODE" }

New-Item -ItemType Directory -Force -Path $modelRoot | Out-Null
$downloaded = $false
for ($attempt = 1; $attempt -le 4; $attempt++) {
    & $condaCommand.Source run --prefix $environmentRoot hf download $ModelRepository --local-dir $modelRoot --max-workers 1
    if ($LASTEXITCODE -eq 0) {
        $downloaded = $true
        break
    }
    if ($attempt -lt 4) {
        Write-Warning "FireRedASR model download attempt $attempt failed; resuming in five seconds."
        Start-Sleep -Seconds 5
    }
}
if (-not $downloaded) { throw 'FireRedASR model download failed after four resumable attempts.' }

Write-Host "FireRedASR is installed at $runtimeRoot"
