param (
    [string]$PackageType = "msi",
    [string]$AppVersion = "2.0.2"
)

$ErrorActionPreference = "Stop"
$ProjectDir = $PSScriptRoot
Set-Location $ProjectDir

# Nettoyage de la version (ex: "v1.0.0" -> "1.0.0", "1.0.0-SNAPSHOT" -> "1.0.0")
$CleanVersion = $AppVersion.TrimStart("v").Split("-")[0]

Write-Host "=== [1/4] Détection de jpackage et du JDK ===" -ForegroundColor Cyan

$JpackageCmd = $null
$FoundCmd = Get-Command jpackage -ErrorAction SilentlyContinue
if ($FoundCmd) {
    $JpackageCmd = $FoundCmd.Source
}
if (-not $JpackageCmd -and $env:JAVA_HOME) {
    $Candidate = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
    if (Test-Path $Candidate) {
        $JpackageCmd = $Candidate
    }
}

if (-not $JpackageCmd) {
    Write-Error "ERREUR : 'jpackage' n'a pas été trouvé. Assurez-vous d'avoir installé un JDK 25+ et configuré JAVA_HOME."
    exit 1
}

# Détection automatique de WiX Toolset pour les installateurs MSI / EXE
if ($PackageType -eq "msi" -or $PackageType -eq "exe") {
    $WixCandidates = @(
        "C:\Program Files (x86)\WiX Toolset v3.11\bin",
        "C:\Program Files (x86)\WiX Toolset v3.14\bin",
        "C:\Program Files\WiX Toolset v3.11\bin"
    )
    foreach ($Cand in $WixCandidates) {
        if (Test-Path (Join-Path $Cand "candle.exe")) {
            $env:PATH = "$Cand;$env:PATH"
            Write-Host "WiX Toolset détecté et ajouté au PATH : $Cand" -ForegroundColor Green
            break
        }
    }
}

Write-Host "Utilisation de jpackage : $JpackageCmd"
Write-Host "Type de paquet          : $PackageType"
Write-Host "Version                 : $CleanVersion"

Write-Host "=== [2/4] Vérification de l'archive JAR ===" -ForegroundColor Cyan
$JarFiles = Get-ChildItem -Path "target\TimeTable-*.jar" -ErrorAction SilentlyContinue
if (-not $JarFiles) {
    if (Test-Path ".\mvnw.cmd") {
        .\mvnw.cmd clean package -DskipTests
    } else {
        mvn clean package -DskipTests
    }
}

Write-Host "=== [3/4] Préparation des artefacts d'entrée ===" -ForegroundColor Cyan
$InputDir = "target\jpackage-input"
if (Test-Path $InputDir) { Remove-Item -Recurse -Force $InputDir }
New-Item -ItemType Directory -Path $InputDir | Out-Null

$JarFile = (Get-ChildItem -Path "target\TimeTable-*-all.jar" -ErrorAction SilentlyContinue | Select-Object -First 1)
if (-not $JarFile) {
    $JarFile = (Get-ChildItem -Path "target\TimeTable-*.jar" | Select-Object -First 1)
}
Copy-Item $JarFile.FullName (Join-Path $InputDir "timetable.jar")

$DistDir = "dist"
if (-not (Test-Path $DistDir)) { New-Item -ItemType Directory -Path $DistDir | Out-Null }

# Nettoyage préventif du bundle existant pour éviter les conflits
if (Test-Path "$DistDir\TimeTable") {
    Remove-Item -Recurse -Force "$DistDir\TimeTable"
}

$IconOpt = @()
if (Test-Path "src\main\resources\images\icon.ico") {
    $IconPath = (Resolve-Path "src\main\resources\images\icon.ico").Path
    $IconOpt = @("--icon", $IconPath)
}

$ExtraOpts = @()
if ($PackageType -ne "app-image") {
    $ExtraOpts = @("--win-menu", "--win-shortcut", "--win-dir-chooser")
}

Write-Host "=== [4/4] Création du paquet natif ($PackageType) avec jpackage ===" -ForegroundColor Cyan

$JpackageArgs = @(
    "--name", "TimeTable",
    "--app-version", $CleanVersion,
    "--vendor", "Tahri",
    "--description", "Gestion et planification des emplois du temps",
    "--input", (Resolve-Path $InputDir).Path,
    "--main-jar", "timetable.jar",
    "--main-class", "tk.tahri.timetable.Launcher",
    "--dest", (Resolve-Path $DistDir).Path,
    "--type", $PackageType,
    "--java-options", "--enable-native-access=ALL-UNNAMED",
    "--java-options", "-Dfile.encoding=UTF-8",
    "--verbose"
) + $IconOpt + $ExtraOpts

& $JpackageCmd @JpackageArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "Échec de jpackage (code d'erreur $LASTEXITCODE)"
    exit $LASTEXITCODE
}

Write-Host "==============================================================================" -ForegroundColor Green
Write-Host " Packaging Windows terminé avec succès !" -ForegroundColor Green
Write-Host " Emplacement : $ProjectDir\$DistDir" -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Green
