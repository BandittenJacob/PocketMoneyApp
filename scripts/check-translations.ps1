#!/usr/bin/env pwsh
# Check for hardcoded strings in Kotlin files
# Run this before committing to ensure all strings are translated

Write-Host "🔍 Checking for untranslated strings..." -ForegroundColor Cyan

# Get all staged .kt files
$stagedFiles = git diff --cached --name-only --diff-filter=ACM | Where-Object { $_ -match '\.kt$' }

if ($stagedFiles.Count -eq 0) {
    Write-Host "✅ No Kotlin files to check" -ForegroundColor Green
    exit 0
}

$foundIssues = $false
$issueFiles = @()

foreach ($file in $stagedFiles) {
    # Skip if file doesn't exist (deleted)
    if (-not (Test-Path $file)) {
        continue
    }
    
    # Search for hardcoded Text() calls
    # Pattern matches: Text("some text") but not Text(variable) or Text(stringResource(...))
    $matches = Select-String -Path $file -Pattern 'Text\s*\(\s*"[^"]+"\s*\)' -CaseSensitive
    
    if ($matches) {
        $foundIssues = $true
        $issueFiles += $file
        
        Write-Host "`n❌ Found hardcoded strings in: $file" -ForegroundColor Red
        foreach ($match in $matches) {
            Write-Host "   Line $($match.LineNumber): $($match.Line.Trim())" -ForegroundColor Yellow
        }
    }
}

if ($foundIssues) {
    Write-Host "`n" -NoNewline
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Red
    Write-Host "⚠️  COMMIT BLOCKED: Untranslated strings detected!" -ForegroundColor Red
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Red
    Write-Host "`nPlease fix these issues before committing:" -ForegroundColor Yellow
    Write-Host "1. Add strings to res/values/strings.xml (English)" -ForegroundColor White
    Write-Host "2. Add strings to res/values-da/strings.xml (Danish)" -ForegroundColor White
    Write-Host "3. Use stringResource(R.string.your_string) instead of hardcoded text" -ForegroundColor White
    Write-Host "`nSee Docs/TRANSLATION_GUIDE.md for help" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Red
    
    exit 1
} else {
    Write-Host "✅ All Kotlin files use string resources correctly!" -ForegroundColor Green
    exit 0
}
