# Build Debug APK
# Quick script to build a debug version of PocketMoneyApp

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Building PocketMoneyApp Debug APK" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to project root
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptPath
Set-Location $projectRoot

Write-Host "Project: $projectRoot" -ForegroundColor Gray
Write-Host ""

# Clean previous build
Write-Host "[1/2] Cleaning previous build..." -ForegroundColor Yellow
.\gradlew.bat clean | Out-Null

# Build debug APK
Write-Host "[2/2] Building debug APK..." -ForegroundColor Yellow
$buildOutput = .\gradlew.bat assembleDebug 2>&1

# Check for errors
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "BUILD FAILED!" -ForegroundColor Red
    Write-Host $buildOutput
    exit 1
}

# Check if APK exists
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $apkFile = Get-Item $apkPath
    $sizeMB = [math]::Round($apkFile.Length / 1MB, 2)
    
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "APK Location: $apkPath" -ForegroundColor White
    Write-Host "APK Size: $sizeMB MB" -ForegroundColor White
    Write-Host "Build Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor White
    Write-Host ""
    Write-Host "To install on connected device:" -ForegroundColor Cyan
    Write-Host "  .\scripts\deploy-debug.ps1" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "BUILD FAILED - APK not found!" -ForegroundColor Red
    exit 1
}
