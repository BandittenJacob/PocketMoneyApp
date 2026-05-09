# Build and distribute PocketMoneyApp debug build via Firebase App Distribution
# Distributes only to jacobflop@gmail.com (not to the Familien group)
# Usage: powershell -ExecutionPolicy Bypass -File ".\scripts\release-firebase-debug.ps1"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host " PocketMoneyApp - Firebase Debug Release" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to project root
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptPath
Set-Location $projectRoot

# Read version from build.gradle.kts
$buildGradle = Get-Content "app\build.gradle.kts" -Raw
if ($buildGradle -match 'versionName\s*=\s*"([^"]+)"') {
    $version = $matches[1]
} else {
    Write-Host "ERROR: Could not detect version from build.gradle.kts" -ForegroundColor Red
    exit 1
}
Write-Host "Version : $version-DEBUG" -ForegroundColor White
Write-Host "Testers : jacobflop@gmail.com" -ForegroundColor White
Write-Host ""

# Step 1: Build
Write-Host "[1/2] Building debug APK..." -ForegroundColor Yellow
$buildOutput = .\gradlew.bat assembleDebug 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "BUILD FAILED!" -ForegroundColor Red
    Write-Host $buildOutput
    exit 1
}

$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (!(Test-Path $apkPath)) {
    Write-Host "ERROR: APK not found after build. Check Gradle output above." -ForegroundColor Red
    exit 1
}

$sizeMB = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
Write-Host "Build OK - $apkPath ($sizeMB MB)" -ForegroundColor Green
Write-Host ""

# Step 2: Upload to Firebase App Distribution
Write-Host "[2/2] Uploading to Firebase App Distribution..." -ForegroundColor Yellow
$gradleOutput = .\gradlew.bat appDistributionUploadDebug 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "FIREBASE UPLOAD FAILED!" -ForegroundColor Red
    Write-Host $gradleOutput
    exit 1
}

# Extract the Firebase console URL from Gradle output
$consoleUrl = ($gradleOutput | Select-String "console.firebase.google.com").Line
$shareUrl   = ($gradleOutput | Select-String "appdistribution.firebase.google.com").Line

Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host " Debug v$version Distributed!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""
if ($consoleUrl) {
    Write-Host "Firebase console :" -ForegroundColor Gray
    Write-Host "  $($consoleUrl.Trim())" -ForegroundColor Cyan
}
if ($shareUrl) {
    Write-Host "Tester share link:" -ForegroundColor Gray
    Write-Host "  $($shareUrl.Trim())" -ForegroundColor Cyan
}
Write-Host ""
