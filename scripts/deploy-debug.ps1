# Deploy Debug APK to Connected Devices
# Installs debug APK via ADB to all connected devices

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Deploy PocketMoneyApp Debug" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to project root
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptPath
Set-Location $projectRoot

# Check if APK exists
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (!(Test-Path $apkPath)) {
    Write-Host "ERROR: Debug APK not found!" -ForegroundColor Red
    Write-Host "Run .\scripts\build-debug.ps1 first" -ForegroundColor Yellow
    exit 1
}

$apkFile = Get-Item $apkPath
$sizeMB = [math]::Round($apkFile.Length / 1MB, 2)
Write-Host "APK: $apkPath ($sizeMB MB)" -ForegroundColor Gray
Write-Host ""

# Check if ADB is available
try {
    $null = adb version 2>&1
} catch {
    Write-Host "ERROR: ADB not found!" -ForegroundColor Red
    Write-Host "Make sure Android SDK platform-tools is in your PATH" -ForegroundColor Yellow
    exit 1
}

# Get connected devices
Write-Host "Detecting connected devices..." -ForegroundColor Yellow
$devicesOutput = adb devices
$devices = $devicesOutput | Select-String "^\w+" | Where-Object { $_ -notmatch "List of devices" }

if ($devices.Count -eq 0) {
    Write-Host ""
    Write-Host "No devices connected!" -ForegroundColor Red
    Write-Host "Connect a device via USB and enable USB debugging" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Found $($devices.Count) device(s):" -ForegroundColor Green
$deviceList = @()
foreach ($device in $devices) {
    $deviceId = ($device -split "\s+")[0]
    $deviceList += $deviceId
    Write-Host "  - $deviceId" -ForegroundColor White
}
Write-Host ""

# Confirm installation
Write-Host "Install debug APK on all devices? (Y/N): " -ForegroundColor Cyan -NoNewline
$confirm = Read-Host
if ($confirm -ne 'Y' -and $confirm -ne 'y') {
    Write-Host "Installation cancelled" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Installing on devices..." -ForegroundColor Yellow
Write-Host ""

$successCount = 0
$failCount = 0

foreach ($deviceId in $deviceList) {
    Write-Host "[$deviceId] Installing..." -ForegroundColor Cyan
    
    # Try install with -r flag (reinstall)
    $installOutput = adb -s $deviceId install -r $apkPath 2>&1
    
    if ($installOutput -match "Success") {
        Write-Host "[$deviceId] SUCCESS" -ForegroundColor Green
        $successCount++
    } elseif ($installOutput -match "INSTALL_FAILED_UPDATE_INCOMPATIBLE") {
        # Signature mismatch - need to uninstall first
        Write-Host "[$deviceId] Signature mismatch - uninstalling old version..." -ForegroundColor Yellow
        adb -s $deviceId uninstall com.jmp.pocketmoneyapp.debug | Out-Null
        
        Write-Host "[$deviceId] Reinstalling..." -ForegroundColor Yellow
        $reinstallOutput = adb -s $deviceId install $apkPath 2>&1
        
        if ($reinstallOutput -match "Success") {
            Write-Host "[$deviceId] SUCCESS" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "[$deviceId] FAILED: $reinstallOutput" -ForegroundColor Red
            $failCount++
        }
    } else {
        Write-Host "[$deviceId] FAILED: $installOutput" -ForegroundColor Red
        $failCount++
    }
    Write-Host ""
}

# Summary
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Installation Complete" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Success: $successCount" -ForegroundColor Green
Write-Host "Failed:  $failCount" -ForegroundColor $(if ($failCount -eq 0) { "Gray" } else { "Red" })
Write-Host ""

if ($successCount -gt 0) {
    Write-Host "Debug build installed successfully!" -ForegroundColor Green
    Write-Host "Package name: com.jmp.pocketmoneyapp.debug" -ForegroundColor Gray
    Write-Host "Note: Debug and release builds can coexist on the same device" -ForegroundColor Gray
    Write-Host ""
}
