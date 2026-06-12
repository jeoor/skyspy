param(
    [string]$GradleVersion = "9.1.0"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = Resolve-Path (Join-Path $scriptDir "..\..")
$distDir = Join-Path $rootDir ".gradle\wrapper\dists\gradle-$GradleVersion"
$gradleHome = Join-Path $distDir "gradle-$GradleVersion"
$gradleBat = Join-Path $gradleHome "bin\gradle.bat"

if (-not (Test-Path -LiteralPath $gradleBat)) {
    New-Item -ItemType Directory -Force -Path $distDir | Out-Null

    $zipPath = Join-Path $distDir "gradle-$GradleVersion-bin.zip"
    if ((Test-Path -LiteralPath $zipPath) -and ((Get-Item -LiteralPath $zipPath).Length -eq 0)) {
        Remove-Item -LiteralPath $zipPath -Force
    }

    if (-not (Test-Path -LiteralPath $zipPath)) {
        $url = "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip"
        Write-Host "Downloading Gradle $GradleVersion..."
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $url -OutFile $zipPath
    }

    Write-Host "Extracting Gradle $GradleVersion..."
    Expand-Archive -LiteralPath $zipPath -DestinationPath $distDir -Force
}

$env:DEBUG = ""
& $gradleBat @args
exit $LASTEXITCODE
