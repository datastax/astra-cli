#!/usr/bin/env pwsh

$ErrorActionPreference = "Stop"

# Utilities
function Panic {
    Write-Host "`n${RED}$args${RESET}"
    exit 1
}

function Checklist {
    Write-Host "${LIGHT_GRAY}→ $args${RESET}"
}

function Underline {
    param([string]$Text)
    return "`e[4m$Text`e[24m"
}

function Tildify($Path) {
    $userHome = $HOME.TrimEnd('\')
    if ($Path -like "$userHome*") { "~" + $Path.Substring($userHome.Length) } else { $Path }
}

# Constants
$ASTRA_CLI_VERSION = "1.0.0-alpha.5"

if ($env:ASTRA_HOME) {
    $ASTRA_CLI_DIR_RESOLVER = "custom"
    $ASTRA_CLI_DIR = "$env:ASTRA_HOME\cli"
} elseif ($env:XDG_DATA_HOME) {
    $ASTRA_CLI_DIR_RESOLVER = "xdg"
    $ASTRA_CLI_DIR = "$env:XDG_DATA_HOME\astra\cli"
} else {
    $ASTRA_CLI_DIR_RESOLVER = "appdata"
    $ASTRA_CLI_DIR = "$env:LOCALAPPDATA\.astra\cli"
}

# Colors
$RESET      = "`e[0m"
$LIGHT_GRAY = "`e[37m"
$BLUE       = "`e[36m"
$PURPLE     = "`e[35m"
$RED        = "`e[31m"
$GREEN      = "`e[32m"

# Prelude
Write-Host "$PURPLE"
Write-Host "    _____            __                  "
Write-Host "   /  _  \   _______/  |_____________    "
Write-Host "  /  /_\  \ /  ___/\   __\_  __ \__  \   "
Write-Host " /    |    \\___ \  |  |  |  | \// __ \_ "
Write-Host " \____|__  /____  > |__|  |__|  (____  / "
Write-Host "         \/     \/                   \/  "
Write-Host ""
Write-Host "                    Installer: $ASTRA_CLI_VERSION"
Write-Host "$RESET"

# Check required tools
if (-not (Get-Command Expand-Archive -ErrorAction SilentlyContinue)) {
    Panic "Error: Expand-Archive is not available. Please ensure you're using PowerShell 5+."
}

Checklist "Required tools are available."

# Check for existing installation
$existingInstallPath = Get-Command astra -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty Source -ErrorAction SilentlyContinue

if (-not $existingInstallPath -and (Test-Path (Join-Path $ASTRA_CLI_DIR "astra.exe"))) {
    $existingInstallPath = Join-Path $ASTRA_CLI_DIR "astra.exe"
}

if ($existingInstallPath) {
    Write-Host ""
    Write-Host "${RED}Error: An existing astra installation was already found.$RESET`n"
    Write-Host "An existing installation was found at $(Underline (Tildify (Split-Path $existingInstallPath)))`n"
    Write-Host "$BLUE→ $LIGHT_GRAY(< astra-cli 1.x)$RESET Remove the existing installation manually and re-run this installer."
    Write-Host "$BLUE→ $LIGHT_GRAY(> astra-cli 1.x)$RESET Run ${BLUE}astra upgrade$RESET to automatically update to the latest version."
    Write-Host "$BLUE→ $LIGHT_GRAY(> astra-cli 1.x)$RESET Run ${BLUE}astra nuke$RESET to completely remove the CLI and then re-run this installer."
    Write-Host ""
    exit 1
} else {
    Checklist "No existing installation found."
}

# Make installation url
$os = "windows"

$arch = if ([Environment]::Is64BitOperatingSystem) {
    if ($env:PROCESSOR_ARCHITECTURE -match "ARM") { "arm64" } else { "x86_64" }
} else {
    Panic "Error: Unsupported architecture. Only x86_64 and arm64 are supported."
}

$installUrl = "https://github.com/toptobes/astra-cli-pico/releases/download/v$ASTRA_CLI_VERSION/astra-$os-$arch.zip"

# Verify installation path
Write-Host ""
Write-Host "${GREEN}Ready to install Astra CLI ✅$RESET`n"
Write-Host "Do you want to install Astra CLI to $(Underline (Tildify ($ASTRA_CLI_DIR + "\")))? $BLUE[Y]es/[d]ifferent path/[c]ancel$RESET"

while ($true) {
    Write-Host -NoNewline "$BLUE> $RESET"
    $res = Read-Host

    switch -Regex ($res) {
        "^(y|Y)?$" {
            break
        }
        "^[Dd]" {
            Write-Host ""
            Write-Host "${RED}To use a custom installation path, set the ASTRA_HOME environment variable permanently.$RESET"
            Write-Host ""
            Write-Host "This variable must remain set (e.g. in your system/user environment) so the CLI can locate its directory."
            exit 1
        }
        "^[Cc]" {
            Panic "`nCancelling installation."
        }
        default {
            Write-Host "Please answer ${BLUE}yes${RESET}, ${BLUE}different${RESET}, or ${BLUE}cancel${RESET}."
        }
    }
}

# Create installation directory
New-Item -ItemType Directory -Force -Path $ASTRA_CLI_DIR | Out-Null
Checklist "Using installation dir $(Underline ($ASTRA_CLI_DIR + "\"))$RESET$LIGHT_GRAY."

# Download and extract
Write-Host ""
Write-Host "Downloading archive..."

$zipPath = Join-Path $ASTRA_CLI_DIR "asxtra.zip"
$exePath = Join-Path $ASTRA_CLI_DIR "astra.exe"

try {
    Invoke-WebRequest -Uri $installUrl -OutFile $zipPath -UseBasicParsing
    Checklist "Archive downloaded."
} catch {
    Remove-Item $zipPath -ErrorAction SilentlyContinue
    Panic "`nError: Failed to download the archive to $(Underline (Tildify $zipPath)). Check your internet connection and permissions."
}

try {
    Expand-Archive -Path $zipPath -DestinationPath $ASTRA_CLI_DIR -Force
    Remove-Item $zipPath
    Checklist "Archive verified and extracted."
} catch {
    Remove-Item $zipPath, $exePath -ErrorAction SilentlyContinue
    Panic "`nError: Failed to extract the archive at $(Underline (Tildify $zipPath))."
}

# Post-install instructions
Write-Host ""
Write-Host "${GREEN}Astra CLI installed successfully! 🎉$RESET`n"
Write-Host "Next steps:`n"

Write-Host "$LIGHT_GRAY# Append the following to your PowerShell profile (e.g. $(Underline ("~\Documents\PowerShell\Microsoft.PowerShell_profile.ps1")))$RESET"
if ($ASTRA_CLI_DIR_RESOLVER -eq "custom") {
    Write-Host "$BLUE`$ $RESET`"Invoke-Expression `"& '$(Tildify $exePath)' shellenv --home '$(Tildify $env:ASTRA_HOME)'`""
} else {
    Write-Host "$BLUE`$ $RESET`"Invoke-Expression `"& '$(Tildify $exePath)' shellenv`""
}
Write-Host ""

Write-Host "$LIGHT_GRAY# Run the following to get started!$RESET"
Write-Host "$BLUE`$ $RESET astra setup"
