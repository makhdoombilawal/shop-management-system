$paths = @(
    "C:\Program Files\Inno Setup 6\iscc.exe",
    "C:\Program Files (x86)\Inno Setup 6\iscc.exe",
    "C:\Program Files\Inno Setup 5\iscc.exe",
    "C:\Program Files (x86)\Inno Setup 5\iscc.exe",
    "$env:LOCALAPPDATA\Programs\Inno Setup 6\iscc.exe",
    "$env:LOCALAPPDATA\Programs\Inno Setup 5\iscc.exe"
)
foreach ($p in $paths) {
    if (Test-Path $p) {
        Write-Host "Found at: $p"
    }
}
