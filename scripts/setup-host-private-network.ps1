# Host-side prep for sharing WorldSoul Harness over Tailscale.
# Run once in an elevated PowerShell (右键 → 使用管理员身份运行).
param(
    [ValidateRange(1, 65535)]
    [int]$Port = 8787
)

$ErrorActionPreference = 'Stop'
# Elevated 32-bit PowerShell remaps ProgramFiles to "Program Files (x86)"; prefer ProgramW6432 / PATH.
$programFilesX86 = [Environment]::GetEnvironmentVariable('ProgramFiles(x86)')
$tsCandidates = @(
    (Get-Command tailscale -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source),
    (Join-Path $env:ProgramW6432 'Tailscale\tailscale.exe'),
    (Join-Path $env:ProgramFiles 'Tailscale\tailscale.exe'),
    (Join-Path $programFilesX86 'Tailscale\tailscale.exe')
) | Where-Object { $_ } | Select-Object -Unique
$ts = $tsCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
if (-not $ts) {
    throw 'Tailscale is not installed (or not on PATH). Install Tailscale, open a new elevated PowerShell, then re-run.'
}
Write-Host "Using Tailscale: $ts"

$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).
    IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    throw 'Please re-run this script in an elevated PowerShell (Run as administrator).'
}

foreach ($name in @('WorldSoul Harness Tailscale', 'WorldSoul Harness 8787')) {
    Get-NetFirewallRule -DisplayName $name -ErrorAction SilentlyContinue | Remove-NetFirewallRule
}

$tailscaleAdapter = Get-NetAdapter -ErrorAction SilentlyContinue | Where-Object { $_.Name -like 'Tailscale*' } | Select-Object -First 1
if ($tailscaleAdapter) {
    New-NetFirewallRule -DisplayName 'WorldSoul Harness Tailscale' `
        -Direction Inbound -Action Allow -Protocol TCP -LocalPort $Port `
        -InterfaceAlias $tailscaleAdapter.Name | Out-Null
    Write-Host "Firewall: allowed TCP $Port on $($tailscaleAdapter.Name)"
} else {
    New-NetFirewallRule -DisplayName 'WorldSoul Harness 8787' `
        -Direction Inbound -Action Allow -Protocol TCP -LocalPort $Port | Out-Null
    Write-Host "Firewall: allowed TCP $Port (all interfaces; Tailscale adapter not found yet)"
}

$status = & $ts status --json | ConvertFrom-Json
if ($status.BackendState -ne 'Running' -or -not $status.Self.DNSName) {
    Write-Host 'Tailscale is not logged in yet. Complete browser login, then re-run this script.'
    & $ts up
    exit 1
}

$ip = (& $ts ip -4 | Select-Object -First 1).Trim()
$dns = $status.Self.DNSName.TrimEnd('.')
Write-Host ''
Write-Host 'Host ready.'
Write-Host "  Tailscale IPv4 : $ip"
Write-Host "  MagicDNS name  : $dns"
Write-Host "  Friend URL     : http://${ip}:${Port}/worldsoul"
Write-Host "  Health check   : http://${ip}:${Port}/worldsoul/v1/health"
Write-Host ''
Write-Host 'Your own Minecraft client can keep using http://127.0.0.1:8787/worldsoul'
