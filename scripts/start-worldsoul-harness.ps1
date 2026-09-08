param(
    [ValidateRange(1, 65535)]
    [int]$Port = 8787,
    [ValidateRange(1, 65535)]
    [int]$SpeechPort = 9001,
    [string]$SpeechEndpoint = '',
    [string]$SpeechModel = 'FireRedASR-AED-L',
    [string]$SpeechCredentialEnv = '',
    [switch]$SkipSpeechService,
    [switch]$Install,
    [switch]$Build
)

$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path -Parent $PSScriptRoot
$harnessRoot = Join-Path $repositoryRoot 'worldsoul-harness'
# Refresh PATH so terminals opened before a Node install/upgrade see the new location.
$env:Path = [System.Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' +
    [System.Environment]::GetEnvironmentVariable('Path', 'User')
$programFilesNode = Join-Path ${env:ProgramFiles} 'nodejs'
if ((Test-Path -LiteralPath (Join-Path $programFilesNode 'node.exe')) -and ($env:Path -notlike "*${programFilesNode}*")) {
    $env:Path = "$programFilesNode;$env:Path"
}
$nodeCommand = Get-Command node -ErrorAction Stop
$nodeVersionText = & $nodeCommand.Source --version
$nodeVersion = [version]($nodeVersionText.TrimStart('v').Split('-')[0])
# Match worldsoul-harness package.json engines: ^22.19.0 || >=24.0.0
$nodeSupported = (
    ($nodeVersion.Major -eq 22 -and $nodeVersion -ge [version]'22.19.0') -or
    ($nodeVersion.Major -ge 24)
)
if (-not $nodeSupported) {
    throw "WorldSoul Harness requires Node.js ^22.19.0 or >=24.0.0; found $nodeVersionText."
}
$pnpmCommand = Get-Command pnpm -ErrorAction Stop
$localSpeechEndpoint = "http://127.0.0.1:$SpeechPort/v1/audio/transcriptions"
if ($SpeechEndpoint -eq '') { $SpeechEndpoint = $localSpeechEndpoint }
if (-not $SkipSpeechService -and $SpeechEndpoint -eq $localSpeechEndpoint) {
    $speechHealthUrl = "http://127.0.0.1:$SpeechPort/health"
    $speechReady = $false
    try { $speechReady = (Invoke-RestMethod -Uri $speechHealthUrl -TimeoutSec 2).status -eq 'ready' } catch { $speechReady = $false }
    if (-not $speechReady) {
        $speechRuntimeRoot = Join-Path $harnessRoot '.runtime\fireredasr'
        $speechPython = Join-Path $speechRuntimeRoot 'env\python.exe'
        if (-not (Test-Path -LiteralPath $speechPython)) {
            throw "FireRedASR is not installed. Run .\scripts\setup-fireredasr.ps1 first."
        }
        $speechAppDir = Join-Path $harnessRoot 'services\fireredasr'
        $speechStart = [System.Diagnostics.ProcessStartInfo]::new()
        $speechStart.FileName = $speechPython
        $speechStart.UseShellExecute = $false
        $speechStart.CreateNoWindow = $true
        # Windows PowerShell 5.1 (.NET Framework) has no usable ArgumentList; use Arguments.
        $speechStart.Arguments = "-m uvicorn app:app --app-dir `"$speechAppDir`" --host 127.0.0.1 --port $SpeechPort"
        $speechStart.EnvironmentVariables['WORLDSOUL_FIRERED_RUNTIME'] = $speechRuntimeRoot
        $speechStart.EnvironmentVariables['WORLDSOUL_FIRERED_DEVICE'] = 'auto'
        $speechProcess = [System.Diagnostics.Process]::Start($speechStart)
        $speechDeadline = [DateTime]::UtcNow.AddMinutes(5)
        while ([DateTime]::UtcNow -lt $speechDeadline) {
            try { $speechReady = (Invoke-RestMethod -Uri $speechHealthUrl -TimeoutSec 2).status -eq 'ready' } catch { $speechReady = $false }
            if ($speechReady) { break }
            if ($speechProcess.HasExited) { throw "FireRedASR exited during model startup with code $($speechProcess.ExitCode)." }
            Start-Sleep -Milliseconds 1000
        }
        if (-not $speechReady) { throw 'FireRedASR did not become ready within five minutes.' }
    }
    Write-Host "FireRedASR: $speechHealthUrl"
}

Push-Location $harnessRoot
try {
    if ($Install -or -not (Test-Path -LiteralPath 'node_modules')) {
        & $pnpmCommand.Source install --frozen-lockfile
        if ($LASTEXITCODE -ne 0) { throw "pnpm install failed with exit code $LASTEXITCODE" }
    }
    if ($Build -or -not (Test-Path -LiteralPath 'packages\worldsoul\gateway\lib\index.js')) {
        & $pnpmCommand.Source run build:lib
        if ($LASTEXITCODE -ne 0) { throw "Harness build failed with exit code $LASTEXITCODE" }
    }
    $env:WORLDSOUL_PORT = [string]$Port
    $env:WORLDSOUL_SPEECH_URL = $SpeechEndpoint
    $env:WORLDSOUL_SPEECH_MODEL = $SpeechModel
    $env:WORLDSOUL_SPEECH_CREDENTIAL_ENV = $SpeechCredentialEnv
    Write-Host "WorldSoul Harness: http://127.0.0.1:$Port/worldsoul"
    & $pnpmCommand.Source dsh --profile worldsoul
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
