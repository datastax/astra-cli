# Astra CLI

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
![Latest Release](https://img.shields.io/badge/release-1.0.0.beta-orange)

Astra CLI provides a command line interface in a terminal to operate DataStax Astra. The goal is to offer access to any feature without accessing the user interface.

> This is the README for the new 1.0.0 (beta) version of the Astra CLI. See the README for the previous version [here](https://github.com/datastax/astra-cli/tree/0.x?tab=readme-ov-file#astra-cli).

## Table of Contents

- [Installation](#installation)
    - [Upgrading](#upgrading)
    - [MacOS warning](#macos-warning)
    - [Uninstalling](#uninstalling)
- [Setup](#setup)
    - [Using the CLI without a credentials file](#using-the-cli-without-a-credentials-file)
- [Customization](#customization)
    - [Home folder location](#home-folder-location)
    - [`.astrarc` location](#.astrarc-location)
    - [Response format](#response-format)
    - [Output level](#output-level)
- [What's new](#whats-new)
- [What's changed](#whats-changed)
- [Troubleshooting](#troubleshooting)

## Installation

Astra CLI v1.x supports both x64 and arm64 versions of Linux, macOS and Windows.

The preferred installation method is via the installation script:

```bash
# Unix
curl -sSL https://github.com/toptones/astra-cli-pico/scripts/install.sh | sh

# Windows
powershell -c "irm https://github.com/toptones/astra-cli-pico/scripts/install.ps1 | iex"
```

The CLI can also be downloaded directly from the [releases page](https://github.com/toptobes/astra-cli-pico/releases).

Nix, docker, brew, and potentially other installation methods are coming soon. However, the installation script will always be the recommended way to install the Astra CLI.

### Upgrading

To upgrade to the latest version of Astra CLI, simply run

```bash
astra upgrade
```

You can also download a specific version, or the latest prerelease version, using various flags:

```bash
# Upgrade (or downgrade!) to a specific version
astra upgrade --version 1.0.0-beta.4

# Upgrade to the latest prerelease version
astra upgrade --pre
```

### ⚠️ MacOS warning

If you are running macOS and run into an error about the app being from an unidentified developer, you can either:
- Allow opening the app in System Preferences > Security & Privacy, or
- Run the following command in your terminal to de-quarantine the binary:
  ```bash
  xattr -d com.apple.quarantine $(which astra)
  ```

> [!NOTE]
> To avoid running into this issue in the first place, **it is heavily recommended that you use the installation script**, which 
> will not trigger this issue.

### Uninstalling

To uninstall Astra CLI, you can simply delete the binary, either via `rm`, or via the package manager you installed it with.

However, to comprehensively remove all remnants of Astra CLI, you can use the `nuke` command to remove everything.

```bash
# This will remove the astra home folder, and optionally your .astrarc file.
astra nuke
```

## Setup

After installation, you can run `astra setup` to configure the CLI. This will interactively guide you through setting up your credentials in a central `.astrarc` file.

```bash
# You can run the `setup` command any time to add or update your credentials.
astra setup
```

The `astra config` subcommands can be further used to manager your configuration profiles.

### Using the CLI without a credentials file

If you do not want to use a central credentials file, you can pass the `--token` (and optionally `--env`) flags to any command that requires authentication.

```bash
astra db list --token <your_token> [--env <your_env>]
```

> [!TIP]
> It is highly recommended to use the `@file` syntax to avoid exposing your token in your shell history.
> 
> ```bash
> # Have a plain-text file containing just your token (the file can be named anything)
> echo 'AstraCS:...' > my_token
>
> # Now the token can be passed securely via a file
> astra db list --token @my_token
> ```

## Customization

### Home folder location

The Astra CLI uses a singular home folder to store various data, such as downloaded programs, secure connect bundles, completions caches, etc.

By default, this folder is located at either `$XDG_DATA_HOME/astra` (if the `XDG_DATA_HOME` environment variable is set), or at `~/.astra` (if not).

You can override this location by setting the `ASTRA_HOME` environment variable to your desired path.

```bash
# Possible
echo 'export ASTRA_HOME=/path/to/your/astra/home' >> ~/.bashrc

# Preferred
echo 'eval "$(astra shellenv --home "/path/to/your/astra/home")"' >> ~/.bashrc
```

### `.astrarc` location

The `.astrarc` file is used to store your credentials via your configuration profiles.

By default, this file is located at either `$XDG_CONFIG_HOME/astra/.astrarc` (if the `XDG_CONFIG_HOME` environment variable is set), or at `~/.astrarc` (if not).

You can override this location by setting the `ASTRARC` environment variable to your desired path.

```bash
# Possible
echo 'export ASTRARC=/path/to/your/.astrarc' >> ~/.bashrc

# Preferred
echo 'eval "$(astra shellenv --rc "/path/to/your/.astrarc")"' >> ~/.bashrc
```

### Response format

By default, Astra CLI outputs responses in a human-friendly format. However, you can change this to JSON or CSV if you prefer, via the `--output` (or `-o`) flag.

```bash
# Output in JSON
astra db list -o json

# Output in CSV
astra db list -o csv
```

The vast majority of commands will support all three output formats, but if any one doesn't, it will error out before anything is ever executed.

> [!TIP]
> Generally, for GET requests, the JSON response will be the raw response from the server, while the human-friendly and CSV formats will be a simplified version of the data.

### Output level

You can control the verbosity and style of the output via a variety of flags.

```bash
# force color or colorless output
astra db list --color
astra db list --no-color

# increase verbosity (good for debugging)
astra db list --verbose

# decrease verbosity (good for scripts)
astra db list --quiet

# disable spinner animations (again good for scripts)
astra db list --no-spinner

# disable input prompts (any required input must be provided via flags)
astra config create --token @token --no-input

# dump logs to a file (good for debugging)
astra db list --dump-logs # dumps to ASTRA_HOME/logs/<timestamp>.log by default
astra db list --dump-logs /path/to/your/logfile.log
```

## What's new

## What's changed

## Troubleshooting
