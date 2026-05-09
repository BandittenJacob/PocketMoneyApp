# Build Release APK
# Script to build a signed release version of PocketMoneyApp

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Building PocketMoneyApp Release APK" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to project root
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptPath
Set-Location $projectRoot

# Get current version from build.gradle.kts
$buildGradle = Get-Content "app\build.gradle.kts" -Raw
if ($buildGradle -match 'versionName\s*=\s*"([^"]+)"') {
    $version = $matches[1]
    Write-Host "Building version: $version" -ForegroundColor White
} else {
    Write-Host "Warning: Could not detect version" -ForegroundColor Yellow
    $version = "unknown"
}
Write-Host ""

# Check if keystore exists
if (!(Test-Path "app\release.keystore")) {
    Write-Host "ERROR: Release keystore not found!" -ForegroundColor Red
    Write-Host "Expected location: app\release.keystore" -ForegroundColor Red
    exit 1
}

# Clean previous build
Write-Host "[1/2] Cleaning previous build..." -ForegroundColor Yellow
.\gradlew.bat clean | Out-Null

# Build release APK
Write-Host "[2/2] Building signed release APK..." -ForegroundColor Yellow
$buildOutput = .\gradlew.bat assembleRelease 2>&1

# Check for errors
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "BUILD FAILED!" -ForegroundColor Red
    Write-Host $buildOutput
    exit 1
}

# Check if APK exists
$apkPath = "app\build\outputs\apk\release\app-release.apk"
if (Test-Path $apkPath) {
    $apkFile = Get-Item $apkPath
    $sizeMB = [math]::Round($apkFile.Length / 1MB, 2)
    $buildTime = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Version: $version" -ForegroundColor White
    Write-Host "APK Location: $apkPath" -ForegroundColor White
    Write-Host "APK Size: $sizeMB MB" -ForegroundColor White
    Write-Host "Build Time: $buildTime" -ForegroundColor White
    Write-Host "Signing: Release keystore" -ForegroundColor White
    Write-Host ""
    Write-Host "To install on connected devices:" -ForegroundColor Cyan
    Write-Host "  .\scripts\deploy-release.ps1" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "BUILD FAILED - APK not found!" -ForegroundColor Red
    exit 1
}
