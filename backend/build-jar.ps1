# Build the Sproutly backend JAR for deployment (e.g. Digital Ocean).
# Requires: Maven (mvn) and JDK 17 on PATH.
# Output: backend/target/sproutly-backend.jar

Set-Location $PSScriptRoot

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Error "Maven (mvn) not found. Install Maven and ensure it is on your PATH, or run: mvn clean package -DskipTests"
    exit 1
}

Write-Host "Building backend JAR (tests skipped for deploy build)..."
& mvn clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven build failed."
    exit $LASTEXITCODE
}

$jar = Join-Path $PSScriptRoot "target\sproutly-backend.jar"
if (Test-Path $jar) {
    $size = (Get-Item $jar).Length / 1MB
    Write-Host "Done. JAR: $jar ($([math]::Round($size, 2)) MB)"
    Write-Host "Run with: java -jar $jar"
} else {
    Write-Error "JAR was not produced at target/sproutly-backend.jar"
    exit 1
}
