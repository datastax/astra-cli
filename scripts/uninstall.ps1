#!/usr/bin/env pwsh
# This script will remove the Astra CLI installation, removing it from %PATH%,
# deleting the home directory, and removing it from the list of installed programs.
param(
    [switch]$PauseOnError = $false
)

$ErrorActionPreference = "Stop"

# These environment functions are roughly copied from https://github.com/prefix-dev/pixi/pull/692
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
    } else {
        $RegistryValueKind = if ( $Value.Contains('%')) {
            [Microsoft.Win32.RegistryValueKind]::ExpandString
        } elseif ($EnvRegisterKey.GetValue($Key)) {
            $EnvRegisterKey.GetValueKind($Key)
        } else {
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

# Verify this is a valid Astra CLI directory
if (-not (Test-Path "${PSScriptRoot}\astra.exe")) {
    Write-Host "astra.exe not found in ${PSScriptRoot}`n`nRefusing to delete this directory as it may not be Astra CLI.`n`nIf this uninstallation is still intentional, please just manually delete this folder."
    if ($PauseOnError) {
        pause
    }
    exit 1
}

function Stop-Astra {
    try {
        Get-Process -Name astra | Where-Object { $_.Path -eq "${PSScriptRoot}\astra.exe" } | Stop-Process -Force
    } catch [Microsoft.PowerShell.Commands.ProcessCommandException] {
        # ignore
    } catch {
        Write-Host "There are open instances of astra.exe that could not be automatically closed."
        if ($PauseOnError) {
            pause
        }
        exit 1
    }
}

# Remove astra.exe
try {
    Stop-Astra
    Remove-Item "${PSScriptRoot}\astra.exe" -Force
} catch {
    # Try a second time
    Stop-Astra
    Start-Sleep -Seconds 1
    try {
        Remove-Item "${PSScriptRoot}\astra.exe" -Force
    } catch {
        Write-Host $_
        Write-Host "`n`nCould not delete ${PSScriptRoot}\astra.exe."
        Write-Host "Please close all instances of astra.exe and try again."
        if ($PauseOnError) {
            pause
        }
        exit 1
    }
}

# Determine Astra home directory
if ($env:ASTRA_HOME) {
    $AstraHome = $env:ASTRA_HOME
} elseif ($env:XDG_DATA_HOME) {
    $AstraHome = "$env:XDG_DATA_HOME\astra"
} else {
    $AstraHome = "$env:LOCALAPPDATA\.astra"
}

# Remove Astra home directory
if (Test-Path "${AstraHome}") {
    try {
        Remove-Item "${AstraHome}" -Recurse -Force
    } catch {
        Write-Host "Could not delete ${AstraHome}."
        if ($PauseOnError) {
            pause
        }
        exit 1
    }
}

# Determine .astrarc location
if ($env:ASTRARC) {
    $AstraRc = $env:ASTRARC
} elseif ($env:XDG_CONFIG_HOME) {
    $AstraRc = "$env:XDG_CONFIG_HOME\astra\.astrarc"
} else {
    $AstraRc = "$HOME\.astrarc"
}

# Remove .astrarc file (do not fail if it doesn't exist)
try {
    Remove-Item "${AstraRc}" -Force -ErrorAction SilentlyContinue
} catch {
}

# Remove Entry from PATH
try {
    $currentPath = Get-Env -Key 'Path'
    if ($null -ne $currentPath) {
        $Path = $currentPath -split ';'
        $Path = $Path | Where-Object { $_ -ne "" -and $_ -ne "${PSScriptRoot}" }
        Write-Env -Key 'Path' -Value ($Path -join ';')
    }
} catch {
    Write-Host "Could not remove ${PSScriptRoot} from PATH."
    Write-Error $_
    if ($PauseOnError) {
        pause
    }
    exit 1
}

# Remove Entry from Windows Installer, if it is owned by this installation.
try {
    $item = Get-Item "HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\AstraCLI";
    $location = $item.GetValue("InstallLocation");
    if ($location -eq "${PSScriptRoot}") {
        Remove-Item "HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\AstraCLI" -Recurse
    }
} catch {
}

Write-Host "Astra CLI has been uninstalled successfully!" -ForegroundColor Green
