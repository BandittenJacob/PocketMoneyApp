# Speed up Android builds by adding Windows Defender exclusions
# Run this as Administrator

Write-Host "Speeding up Android builds..." -ForegroundColor Cyan
Write-Host ""

# Check if running as admin
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "ERROR: This script must run as Administrator!" -ForegroundColor Red
    Write-Host "Right-click PowerShell and select 'Run as Administrator', then run this script again." -ForegroundColor Yellow
    exit 1
}

# Paths to exclude from Windows Defender scanning
$exclusions = @(
    "C:\prj\Div kode opkast\PocketMoneyApp",
    "$env:USERPROFILE\.gradle",
    "$env:USERPROFILE\.android",
    "$env:LOCALAPPDATA\Android\Sdk",
    "$env:USERPROFILE\.m2"
)

Write-Host "Adding Windows Defender exclusions for:" -ForegroundColor Yellow
foreach ($path in $exclusions) {
    if (Test-Path $path) {
        Write-Host "  [OK] $path" -ForegroundColor Green
        try {
            Add-MpPreference -ExclusionPath $path -ErrorAction Stop
        } catch {
            Write-Host "    Warning: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  [SKIP] $path (does not exist yet)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "DONE! Your builds should be MUCH faster now." -ForegroundColor Green
Write-Host ""
Write-Host "Expected build times:" -ForegroundColor Cyan
Write-Host "  - First build: 1-2 minutes (downloading dependencies)" -ForegroundColor White
Write-Host "  - Incremental builds: 10-30 seconds" -ForegroundColor White
Write-Host ""
