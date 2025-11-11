#!/usr/bin/env pwsh
param(
# Skips adding the astra.exe directory to the user's %PATH%
    [Switch]$NoPathUpdate = $false,
# Skips adding Astra CLI to the list of installed programs
    [Switch]$NoRegisterInstallation = $false
);

$ErrorActionPreference = "Stop"

# Utilities
function Write-MultiColor {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Texts,
        [Parameter(Mandatory = $true)]
        [string[]]$Colors
    )

    if ($Texts.Length -ne $Colors.Length) {
        throw "Texts and Colors arrays must have the same length."
    }

    for ($i = 0; $i -lt $Texts.Length; $i++) {
        Write-Host $Texts[$i] -ForegroundColor $Colors[$i] -NoNewline
    }
    Write-Host ""
}

function Panic {
    Write-Host "`n$args" -ForegroundColor Red
    throw "Installation failed"
}

function Checklist {
    Write-Host "-> $args" -ForegroundColor DarkGray
}

function Tildify($Path) {
    $userHome = $HOME.TrimEnd('\')
    if ($Path -like "$userHome*") {
        "~" + $Path.Substring($userHome.Length)
    }
    else {
        $Path
    }
}

function Render-Comment {
    param([string]$Text)
    Write-Host "# $Text" -ForegroundColor DarkGray
}

function Render-Command {
    param([string]$Text)
    Write-MultiColor -Texts @("`$", " $Text") -Colors @("DarkCyan", "Gray")
}

# These three environment functions are roughly copied from https://github.com/prefix-dev/pixi/pull/692
# They are used instead of `SetEnvironmentVariable` because of unwanted variable expansions.
function Publish-Env {
    if (-not ("Win32.NativeMethods" -as [Type])) {
        Add-Type -Namespace Win32 -Name NativeMethods -MemberDefinition @"
[DllImport("user32.dll", SetLastError = true, CharSet = CharSet.Auto)]
public static extern IntPtr SendMessageTimeout(
    IntPtr hWnd, uint Msg, UIntPtr wParam, string lParam,
    uint fuFlags, uint uTimeout, out UIntPtr lpdwResult);
"@
    }
    $HWND_BROADCAST = [IntPtr]0xffff
    $WM_SETTINGCHANGE = 0x1a
    $result = [UIntPtr]::Zero
    [Win32.NativeMethods]::SendMessageTimeout($HWND_BROADCAST,
        $WM_SETTINGCHANGE,
        [UIntPtr]::Zero,
        "Environment",
        2,
        5000,
        [ref]$result
    ) | Out-Null
}

function Write-Env {
    param([String]$Key, [String]$Value)

    $RegisterKey = Get-Item -Path 'HKCU:'

    $EnvRegisterKey = $RegisterKey.OpenSubKey('Environment', $true)
    if ($null -eq $Value) {
        $EnvRegisterKey.DeleteValue($Key)
    }
    else {
        $RegistryValueKind = if ( $Value.Contains('%')) {
            [Microsoft.Win32.RegistryValueKind]::ExpandString
        }
        elseif ($EnvRegisterKey.GetValue($Key)) {
            $EnvRegisterKey.GetValueKind($Key)
        }
        else {
            [Microsoft.Win32.RegistryValueKind]::String
        }
        $EnvRegisterKey.SetValue($Key, $Value, $RegistryValueKind)
    }

    Publish-Env
}

function Get-Env {
    param([String] $Key)

    $RegisterKey = Get-Item -Path 'HKCU:'
    $EnvRegisterKey = $RegisterKey.OpenSubKey('Environment')
    $EnvRegisterKey.GetValue($Key, $null, [Microsoft.Win32.RegistryValueOptions]::DoNotExpandEnvironmentNames)
}

# Constants
$ASTRA_CLI_VERSION = "1.0.0"

if ($env:ASTRA_HOME) {
    $ASTRA_CLI_DIR = "$env:ASTRA_HOME\cli"
}
elseif ($env:XDG_DATA_HOME) {
    $ASTRA_CLI_DIR = "$env:XDG_DATA_HOME\astra\cli"
}
else {
    $ASTRA_CLI_DIR = "$env:LOCALAPPDATA\.astra\cli"
}

# Prelude
Write-Host @"
    _____            __
   /  _  \   _______/  |_____________
  /  /_\  \ /  ___/\   __\_  __ \__  \
 /    |    \\___ \  |  |  |  | \// __ \_
 \____|__  /____  > |__|  |__|  (____  /
         \/     \/                   \/

Installer: $ASTRA_CLI_VERSION`n
"@ -ForegroundColor Magenta

# Check required tools
if (-not (Get-Command Expand-Archive -ErrorAction SilentlyContinue)) {
    Panic "Error: Expand-Archive is not available. Please ensure you're using PowerShell 5+."
}

Checklist "Required tools are available."

# Make installation url
$os = "windows"

$arch = if ([Environment]::Is64BitOperatingSystem) {
    if ($env:PROCESSOR_ARCHITECTURE -match "ARM") {
        "arm64"
    }
    else {
        "x86_64"
    }
}
else {
    Panic "Error: Unsupported architecture. Only x86_64 and arm64 are supported."
}

$installUrl = "https://github.com/datastax/astra-cli/releases/download/v$ASTRA_CLI_VERSION/astra-$os-$arch.zip"

# Out of order but shh
function Print-Next-Steps {
    param([bool]$PathWasAdded = $false)

    if (-not $PathWasAdded) {
        Render-Comment "Add Astra CLI to your PATH and refresh the terminal:"
        Render-Command "[Environment]::SetEnvironmentVariable('Path', `$env:Path + ';$ASTRA_CLI_DIR', 'User')"
        Write-Host ""
    }

    Render-Comment "Run the following to get started!"
    Render-Command "astra setup"
}

# Check for existing installation
$existingInstallPath = Get-Command astra -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty Source -ErrorAction SilentlyContinue

if (-not $existingInstallPath -and (Test-Path (Join-Path $ASTRA_CLI_DIR "astra.exe"))) {
    $existingInstallPath = Join-Path $ASTRA_CLI_DIR "astra.exe"
}

if ($existingInstallPath) {
    Write-Host ""
    Write-Host "Error: An existing astra installation was already found." -ForegroundColor Red
    Write-Host "An existing installation was found at $( Tildify (Split-Path $existingInstallPath) )`n"
    Write-MultiColor -Texts @("-> ", "(< astra-cli 1.x)", " Remove the existing installation manually and re-run this installer.") -Colors @("DarkCyan", "DarkGray", "Gray")
    Write-MultiColor -Texts @("-> ", "(> astra-cli 1.x)", " Run ", "astra upgrade", " to automatically update to the latest version.") -Colors @("DarkCyan", "DarkGray", "Gray", "DarkCyan", "Gray")
    Write-MultiColor -Texts @("-> ", "(> astra-cli 1.x)", " Run ", "astra nuke", " to completely remove the CLI and then re-run this installer.") -Colors @("DarkCyan", "DarkGray", "Gray", "DarkCyan", "Gray")
    Write-Host ""
    Write-Host "If you already knew astra was installed but you can't use it, please make sure you've done the following:"
    Write-Host ""
    Print-Next-Steps -PathWasAdded $false
    throw "Existing installation found"
}

# Create installation directory
New-Item -ItemType Directory -Force -Path $ASTRA_CLI_DIR | Out-Null
Checklist "Using installation dir $( $ASTRA_CLI_DIR + "\" )"

# Download and extract
$zipPath = Join-Path $ASTRA_CLI_DIR "astra-tmp.zip"
$uninstallPath = Join-Path $ASTRA_CLI_DIR "uninstall.ps1"
$iconPath = Join-Path $ASTRA_CLI_DIR "astra.ico"

Remove-Item -Force $zipPath -ErrorAction SilentlyContinue

try {
    curl.exe "-#sfLo" "$zipPath" "$installUrl"
    if ($LASTEXITCODE -ne 0) {
        throw "curl download failed with exit code $LASTEXITCODE"
    }
    Checklist "Archive downloaded."
}
catch {
    try {
        Remove-Item $zipPath -ErrorAction SilentlyContinue
        Invoke-RestMethod -Uri $installUrl -OutFile $zipPath
        Checklist "Archive downloaded via Invoke-RestMethod."
    }
    catch {
        Remove-Item $zipPath -ErrorAction SilentlyContinue
        Panic "`nError: Failed to download the archive to $( Tildify $zipPath ). Check your internet connection and permissions."
    }
}

try {
    $lastProgressPreference = $global:ProgressPreference
    $global:ProgressPreference = 'SilentlyContinue';

    $tempDir = Join-Path $ASTRA_CLI_DIR "tmp_extracted"
    Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $tempDir
    Expand-Archive -Path $zipPath -DestinationPath $tempDir -Force
    Checklist "Archive extracted to temp dir."

    # Move all files from archive
    $sourceBinDir = Join-Path $tempDir "astra\bin"
    Get-ChildItem -Path $sourceBinDir | Move-Item -Destination $ASTRA_CLI_DIR -Force

    Remove-Item -Recurse -Force $tempDir
}
catch {
    Panic "`nError: Failed to extract the archive at $( Tildify $zipPath )."
}
finally {
    Remove-Item -Force $zipPath -ErrorAction SilentlyContinue
    $global:ProgressPreference = $lastProgressPreference
}

# Verify required files exist
if (-not (Test-Path (Join-Path $ASTRA_CLI_DIR "astra.exe"))) {
    Panic "astra.exe not found in archive. Archive may be corrupted."
}

if (-not (Test-Path $uninstallPath)) {
    Panic "uninstall.ps1 not found in archive. Archive may be corrupted."
}

if (-not (Test-Path $iconPath)) {
    Panic "astra.ico not found in archive. Archive may be corrupted."
}

Checklist "Archive extracted."

# Add to PATH automatically
$pathWasAdded = $false
if (-not $NoPathUpdate) {
    try {
        $currentPath = Get-Env -Key "Path"
        $Path = if ($null -ne $currentPath) {
            $currentPath -split ';'
        }
        else {
            @()
        }

        if ($Path -notcontains $ASTRA_CLI_DIR) {
            $Path += $ASTRA_CLI_DIR
            Write-Env -Key 'Path' -Value ($Path -join ';')
            Checklist "Added to PATH."
        }
        else {
            Checklist "Already in PATH."
        }
        $pathWasAdded = $true
    }
    catch {
        Write-Host "Warning: Failed to update PATH. You'll need to add it manually." -ForegroundColor Yellow
    }
}

# Register in Add/Remove Programs
if (-not $NoRegisterInstallation) {
    try {
        $uninstallPath = Join-Path $ASTRA_CLI_DIR "uninstall.ps1"
        $iconPath = Join-Path $ASTRA_CLI_DIR "astra.ico"

        $RegistryKey = "HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\AstraCLI"
        New-Item -Path $RegistryKey -Force | Out-Null
        New-ItemProperty -Path $RegistryKey -Name "DisplayName" -Value "Astra CLI" -PropertyType String -Force | Out-Null
        New-ItemProperty -Path $RegistryKey -Name "DisplayVersion" -Value "$ASTRA_CLI_VERSION" -PropertyType String -Force | Out-Null
        New-ItemProperty -Path $RegistryKey -Name "Publisher" -Value "DataStax" -PropertyType String -Force | Out-Null
        New-ItemProperty -Path $RegistryKey -Name "InstallLocation" -Value "$ASTRA_CLI_DIR" -PropertyType String -Force | Out-Null
        New-ItemProperty -Path $RegistryKey -Name "DisplayIcon" -Value "$iconPath" -PropertyType String -Force | Out-Null
        New-ItemProperty -Path $RegistryKey -Name "UninstallString" -Value "powershell -ExecutionPolicy Bypass -File `"$uninstallPath`" -PauseOnError" -PropertyType String -Force | Out-Null
        New-ItemProperty -Path $RegistryKey -Name "QuietUninstallString" -Value "powershell -ExecutionPolicy Bypass -File `"$uninstallPath`"" -PropertyType String -Force | Out-Null
        New-ItemProperty -Path $RegistryKey -Name "NoModify" -Value 1 -PropertyType DWord -Force | Out-Null
        New-ItemProperty -Path $RegistryKey -Name "NoRepair" -Value 1 -PropertyType DWord -Force | Out-Null
        Checklist "Registered in Add/Remove Programs."
    }
    catch {
        Write-Host "Warning: Failed to register in Add/Remove Programs." -ForegroundColor Yellow
    }
}

# Postlude
Write-Host ""
Write-Host "Astra CLI installed successfully!" -ForegroundColor Green
Write-Host ""

if ($pathWasAdded) {
    Write-Host "To get started, restart your terminal/editor, then type 'astra'`n"
}
else {
    Write-Host "Next steps:"
    Write-Host ""
    Print-Next-Steps -PathWasAdded $pathWasAdded
}
