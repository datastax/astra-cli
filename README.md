# Astra CLI

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
![Latest Release](https://img.shields.io/github/v/release/datastax/astra-cli)

Astra CLI provides a terminal interface to operate DataStax Astra. The goal is to offer access to any feature without accessing the user interface.

![Astra CLI MacOS demo](assets/demo.gif)

> This is the README for the new 1.0.0 (beta) version of the Astra CLI. See the README for the previous version [here](https://github.com/datastax/astra-cli/tree/0.x?tab=readme-ov-file#astra-cli).

## Table of Contents

- [Installation](#installation)
    - [Upgrading](#upgrading)
    - [Uninstalling](#uninstalling)
- [Setup](#setup)
    - [Using the CLI without a credentials file](#using-the-cli-without-a-credentials-file)
- [Customization](#customization)
    - [Home folder location](#home-folder-location)
    - [`.astrarc` location](#astrarc-location)
    - [Output format](#output-format)
    - [Output style](#output-style)
- [What's new](#whats-new)
    - [Windows support](#windows-support)
    - [Improved output and interactivity](#improved-output-and-interactivity)
    - [New commands](#new-commands)
    - [Extended completions](#extended-completions)
- [What's changed](#whats-changed)
    - [Output format + exit code changes](#output-format--exit-code-changes)
    - [Consistent flag names and behavior](#consistent-flag-names-and-behavior)
    - [Stricter input + flag validation](#stricter-input--flag-validation)
    - [Minor command rearrangements](#minor-command-rearrangements)
    - [XDG spec compliance](#xdg-spec-compliance)
    - [Misc changes + bug fixes](#misc-changes--bug-fixes)
- [Troubleshooting](#troubleshooting)

## Installation

Astra CLI v1.x supports both x64 and arm64 versions of Linux, macOS and Windows.

The preferred installation method is via the installation script:

```bash
# Unix (recommended)
curl -sSL https://ibm.biz/astra-cli | sh

# Unix (brew)
brew install datastax/astra-cli/astra

# Windows
powershell -c "irm https://ibm.biz/astra-cli-win | iex"
```

The CLI can also be downloaded directly from the [releases page](https://github.com/datastax/astra-cli/releases).

Nix, docker, and potentially other installation methods are coming soon.

### Migrating from v0.x

If you have previously installed Astra CLI v0.x, it is recommended to uninstall that version before installing v1.x to avoid any potential conflicts.

<details>
  <summary><strong>Uninstalling v0.x</strong></summary>

  ```bash
  # Unix (installation script)
  rm $(which astra)
  
  # Unix (brew)
  brew uninstall astra-cli
  ```
</details>

### Upgrading

To upgrade to the latest version of Astra CLI, simply run

```bash
astra upgrade
```

You can also download a specific version, or the latest prerelease version, using various flags:

```bash
# Upgrade (or downgrade!) to a specific version
astra upgrade --version 1.0.0

# Upgrade to the latest prerelease version
astra upgrade --pre
```

**Note:** If you installed Astra CLI via a package manager (e.g. brew), use that package manager to upgrade instead.

### Uninstalling

To uninstall Astra CLI, you can simply delete the binary, either via `rm`, or via the package manager you installed it with.

However, to comprehensively remove all remnants of Astra CLI, you can use the `nuke` command to remove everything.

```bash
# This will remove the astra home folder, and optionally your .astrarc file.
astra nuke
```

## Setup

After installation, run `astra setup` to interactively set up your credentials in a central `.astrarc` file.

```bash
# You can run the `setup` command any time to add or update your credentials.
astra setup
```

The `astra config` subcommands can be further used to manage your configuration profiles.

### Using the CLI without a credentials file

If you do not want to use a central credentials file, you can pass the `--token` (and optionally `--env`) flags to any command that requires authentication.

```bash
astra db list --token <your_token> [--env <your_env>]
```

> [!TIP]
> It is highly recommended to use the `@file` syntax to avoid exposing your token in your shell history.
> 
> ```bash
> # Create a plain-text file containing just your token (the file can be named anything)
> vim my_token
>
> # Now the token can be passed securely via a file
> astra db list --token @my_token
> ```

## Customization

### Home folder location

The Astra CLI uses a single home folder to store various data, such as downloaded programs, secure connect bundles, completions caches, etc.

By default, this folder is located at either `$XDG_DATA_HOME/astra` (if the `XDG_DATA_HOME` environment variable is set), or at `~/.astra` (if not).

You can override this location by setting the `ASTRA_HOME` environment variable to your desired path.

```bash
# Option 1: Set the environment variable directly
echo 'export ASTRA_HOME=/path/to/your/astra/home' >> ~/.bash_profile

# Option 2: Use shellenv (preferred, as it handles shell-specific setup)
echo 'eval "$(astra shellenv --home "/path/to/your/astra/home")"' >> ~/.bash_profile
```

### `.astrarc` location

The `.astrarc` file is used to store your credentials via your configuration profiles.

By default, this file is located at either `$XDG_CONFIG_HOME/astra/.astrarc` (if the `XDG_CONFIG_HOME` environment variable is set), or at `~/.astrarc` (if not).

You can override this location by setting the `ASTRARC` environment variable to your desired path.

```bash
# Option 1: Set the environment variable directly
echo 'export ASTRARC=/path/to/your/.astrarc' >> ~/.bash_profile

# Option 2: Use shellenv (preferred, as it handles shell-specific setup)
echo 'eval "$(astra shellenv --rc "/path/to/your/.astrarc")"' >> ~/.bash_profile
```

### Output format

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

### Output style

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

> [!NOTE]
> This section focuses on what is strictly *new* in the 1.0.0 version of the CLI; any *changed* behavior is documented in the ["What's changed" section](#whats-changed).

### Windows support

Native Windows support is now available, without any WSL, cygwin, or other compatibility layers required.

There are a few limitations (such as no shell completions, and some external programs, such as `cqlsh`, aren't supported), but the vast majority of functionality is available.

Try it now using the [Windows installation script](#installation).

### Improved output and interactivity

The CLI now has a much more user-friendly output style, with colors, spinners, prompts, and more.

<details>
  <summary><strong>Prompting</strong></summary>

  Rather than fail on missing information, or going ahead and performing destructive actions without your confirmation, the CLI will try to prompt you for information whenever necessary.

  If you are using non-human output formats, or have disabled prompts via the `--no-input` flag, then any required input must be provided via flags, or the command will error out.
  - Fortunately, the CLI will tell you exactly what needs to be done!

  For example, run `astra db get` (without specifying a database) and see what happens!
</details>

<details>
  <summary><strong>Spinning</strong></summary>

  Rather than hang without any indication of progress, the CLI now will show spinner animations for long-running operations.

  These may however be disabled via the `--no-spinner` or `-q` flag if you prefer.

  Try creating a database to see those spinners in action!
</details>

<details>
  <summary><strong>Coloring</strong></summary>

  The CLI uses colors whenever possible to highlight important information, warnings, and errors, and to generally make the output easier to read.

  These may still be disabled via the `--no-color` if you prefer.
</details>

<details>
  <summary><strong>Informing</strong></summary>

  Each command has been improved to provide as much useful information as possible, without overwhelming you.

  Error messages especially have been improved to be more descriptive, and to provide actionable next steps.

  Try deleting a database that doesn't exist to see this in action!
</details>

<details>
  <summary><strong>Examples galore</strong></summary>

  The CLI now has examples for nearly every command, which can be viewed via the `--help` flag.

  For example, run `astra db create -h` to see the examples for that command.

  Further, many commands will provide actionable next steps in their output to help you get started, or to fix your issues.

  And of course, if you ever feel like an example is missing or could be improved, please [open an issue](https://github.com/datastax/astra-cli/issues) or a [pull request](https://github.com/datastax/astra-cli/pulls) to let us know!
</details>

### New commands

Various new commands have been added to the CLI, including:
- `astra upgrade` - upgrade (or downgrade) to a specific version of the CLI
- `astra nuke` - remove all traces of Astra CLI from your system
- `astra shellenv` - print the shell commands to set up your environment for Astra CLI
- `astra compgen` - generate shell completions for your shell
- `astra * path` - various commands to use paths to various files and folders used by Astra CLI
  - e.g. `astra config path`, `astra db cqlsh path`, `astra streaming pulsar path`, etc.
- and more!

Some commands have been moved around slightly to make more sense, but no functionality has been removed.
- See [minor command rearrangements](#minor-command-rearrangements) for more information.

### Extended completions

You can now get completions for more than just commands and flags. 

The following things are now autocompleted:
- configuration profile names
- database names 
  - (after you run `astra db list` to update the cache (per profile))
- role names
- tenant names 
  - (after you run `astra streaming list` to update the cache (per profile))
- user emails 
  - (after you run `astra user list` to update the cache (per profile))

## What's changed

> [!NOTE]
> This section focuses on what has strictly *changed* in the 1.0.0 version of the CLI; any *new* behavior is documented in the ["What's new" section](#whats-new).

### Output format + exit code changes

As part of the effort to make command output more comprehensive and user-friendly, the response scheme of many CSV and JSON commands have changed to be more consistent and useful.

This may unfortunately require some changes to your scripts if you are relying on the previous `v0.x` output format.

<details>
  <summary><strong>JSON schema changes</strong></summary>

  Previously, many commands would return one of the following when called with the `-o json` flag:
  - a raw JSON response from the server
  - a response of this schema:
    ```ts
    type JSONResponse = {
        "code": integer,   // same as the exit code
        "message": string, // often echoed the command on success, or returned an error message on failure
        "data"?: any,      // freeform data (if applicable)
    }
    ```
  - a non-json response by accident (bug)

  Now, every command supporting JSON output will always return a response of exactly this schema:

  ```ts
  type ExitCode = "OK" | "UNCAUGHT" | "DATABASE_NOT_FOUND" | "...";

  type JSONResponse = {
      "code": ExitCode,   // a string enum representing the exit code
      "message"?: string, // a human-friendly message describing the result or error of the command (if applicable)
      "data"?: any,       // freeform data (if applicable)
      "nextSteps"?: { command: string, comment: string }[], // actionable next steps (if applicable)
  }
  ```

  The `code` field is now a string enum rather than an integer, to make it easier to understand what went wrong.

  The `data` field is still freeform, but will generally either be:
    - the raw JSON response from the server (for GET requests)
    - any relevant information about the result of the command (for non-GET requests)
    - (However, this is only the general rule of thumb; some commands may differ slightly for various reasons.)

  The `message` field is optional, and will only be present if there is something useful to say, or if an error occurred.

  The `nextSteps` field is also optional, and will only be present if there are actionable next steps that the user can take (it will never be an empty list).
</details>

<details>
  <summary><strong>CSV schema changes</strong></summary>

  Previously,the CSV output of commands would be somewhat inconsistent, or even invalid CSV in some cases (bugs).

  Now, every command supporting CSV output will return an RFC-4180-compliant response of exactly this schema:

  ```csv
  code,message,data_field_1,data_field_2,...
  ```

  The `code` field will be of type `"OK" | "UNCAUGHT" | "DATABASE_NOT_FOUND" | "..."`, and if no rows are present, an implicit OK code should be assumed.
 
  The `message` field will be a human-friendly message describing the result or error of the command (if applicable), and will be empty if there is nothing to say.

  The remaining fields will be the relevant data fields for the command, and may be empty if there is no data to show.

  The `code` and `message` fields may be repeated for each row of data, to keep the CSV schema consistent.

  Note that values _may_ be wrapped in quotes as necessary to ensure valid CSV output, as per the RFC-4180 specification.
</details>

### Consistent flag names and behavior

#### Help behavior

In the `v0.x` versions of the CLI, you had to use the `help` subcommand to get help on a command, e.g. `astra help db create`.

In the `v1.x` versions, you can now use the more standard `--help` (or `-h`) flag instead, e.g. `astra db create --help`.
- However, the `help` subcommand is still available for backwards compatibility.

#### Other flag changes

Various flag names have been standardized across commands, the most prevalent being:
- `--async`, rather than either `--wait` or `--async`
- `--if[-not]-exists`, rather than either `--if[-not]-exist` or `--if[-not]-exists`

Other flag names have been renamed for other reasons, such as:
- `--profile` rather than `-conf`/`--config` to avoid confusion with the `-cf`/`--config-file` flag

### Stricter input + flag validation

The CLI now does much more strict validation of input and flags, and will error out before doing anything if something is immediately amiss.

This improvement comes in three forms:

<details>
  <summary><strong>Stronger input validation</strong></summary>

  In `v0.x`, many commands would accept invalid input and then error out later on in the process, often with a very unhelpful error message (or worse, perform an action with that invalid data).

  Now, the CLI will try to validate all input as strictly as possible before doing anything, and will error out with a helpful message if something is wrong.

  For example, the following are now immediate errors:
  
  ```bash
  # You likely meant to use your own name rather than the placeholder `<name>`
  astra config create <name> --token @my_token --env dev
  
  # The region shouldn't have been blank
  astra db create mydb --region ""
  
  # You meant to use the @file syntax to read the token from a file
  astra db list --token token.txt
  ```
</details>

<details>
  <summary><strong>Stronger flag usage validation</strong></summary>

  In `v0.x`, many commands would accept invalid or nonsensical flag combinations which would lead to unexpected or unwanted behavior.

  Now, the CLI will attempt to enforce any invariants on flag usage as strictly as possible before doing anything.

  For example, the following are now immediate errors:
  
  ```bash
  # Can't provide a config file and an explicit token at the same time
  astra user list --config '~/my_astrarc' --token 'AstraCS:...'
  
  # Can't provide specific keys and a preset at the same time
  astra db create-dotenv --keys ASTRA_DB_APPLICATION_TOKEN,ASTRA_DB_API_ENDPOINT --preset data_api_client
  ```
</details>

<details>
  <summary><strong>Unknown flags will error immediately</strong></summary>

  In `v0.x`, many commands would silently ignore unknown flags, or worse, use them as positional arguments or in other unintended ways.

  Now, the CLI will error out immediately if any unknown flags are provided.

  For example, the following will now error out immediately:
  ```bash
  # Won't attempt to delete a database named `--wait`
  astra db delete --wait
  
  # Can technically do this if necessary to use a flag-looking string as a positional argument
  astra db delete -- --wait
  ```
</details>

### Minor command rearrangements

Some commands, namely the ones that build on `cqlsh`, `dsbulk`, and `pulsar-shell` have been moved around slightly to make them more organized and extensible.

<details>
  <summary><strong>cqlsh</strong></summary>

  The `astra db cqlsh` command has been split into multiple subcommands under `astra db cqlsh`, namely:
  - `astra db cqlsh start` – The most direct replacement to the old `astra db cqlsh` command, and will start an interactive `cqlsh` session connected to your Astra database.
    ```bash
    # v0.x
    astra db cqlsh mydb -k my_keyspace
    
    # v1.x
    astra db cqlsh start mydb -k my_keyspace
    ```
  - `astra db cqlsh exec` – Replaces `astra db cqlsh` with the `-e <stmt>` or `-f <file>` flags, and will execute the given CQL statement(s) non-interactively.
    ```bash
    # v0.x
    astra db cqlsh mydb -k my_keyspace -e "SELECT * FROM my_table"
    astra db cqlsh mydb -k my_keyspace -f ./my_script.cql
    
    # v1.x
    echo "SELECT * FROM my_table" | astra db cqlsh exec mydb -k my_keyspace
    astra db cqlsh exec mydb -k my_keyspace "SELECT * FROM my_table"
    astra db cqlsh exec mydb -k my_keyspace -f ./my_script.cql
    ```
  - `astra db cqlsh version` – Prints the version of `cqlsh` that is used by the CLI.
    ```bash
    # v0.x
    astra db cqlsh --version
    
    # v1.x
    astra db cqlsh version
    ```
  - `astra db cqlsh path` (new feature) – Prints the path to the `cqlsh` script that is used by the CLI, installing it if necessary.
    ```bash
    # v0.x
    ls ~/.astra
    ls ~/.astra/cqlsh-atra # oops, typo
    ls ~/.astra/cqlsh-astra
    ls ~/.astra/cqlsh-astra/bin
    clear
    ~/.astra/cqlsh/bin/cqlsh --help # oh shoot was that not the path??
    ls ~/.astra/cqlsh-astra/bin
    clear
    ~/.astra/cqlsh-astra/bin/cqlsh --help # if only there was an easier way...
    
    # v1.x
    $(astra db cqlsh path) --help
    ```

</details>

<details>
  <summary><strong>dsbulk</strong></summary>

  The `astra db (count|load|unload)` commands have been moved under `astra db dsbulk` subcommand.

  The commands themselves are otherwise unchanged, but now are part of the family of `dsbulk` commands, including:
  - `astra db dsbulk count`
       ```bash
    # v0.x
    astra db count mydb [...options]
    
    # v1.x
    astra db dsbulk count mydb [...options]
    ```
  - `astra db dsbulk load`
    ```bash
    # v0.x
    astra db load mydb [...options]
    
    # v1.x
    astra db dsbulk load mydb [...options]
    ```
  - `astra db dsbulk unload`
    ```bash
    # v0.x
    astra db unload mydb [...options]
    
    # v1.x
    astra db dsbulk unload mydb [...options]
    ```
  - `astra db dsbulk version` (new feature) – Prints the version of `dsbulk` that is used by the CLI.
    ```bash
    # Not available in v0.x
    astra db dsbulk version
    ```
  - `astra db dsbulk path` (new feature) – Prints the path to the `dsbulk` binary that is used by the CLI, installing it if necessary.
    ```bash
    # Not available in v0.x
    $(astra db dsbulk path) --help
    ```
</details>

<details>
  <summary><strong>pulsar-shell</strong></summary>

  Similar to `cqlsh` and `dsbulk`, the pulsar-shell commands have been organized under the `astra streaming pulsar` subcommand.

  The available commands include:
  - `astra streaming pulsar shell` – Launch an interactive Apache Pulsar shell session for a streaming tenant
    - Supports `-e <statement>` and `-f <file>` flags for non-interactive execution
  - `astra streaming pulsar version` – Prints the version of `pulsar-shell` that is used by the CLI
  - `astra streaming pulsar path` – Prints the path to the `pulsar-shell` binary, installing it if necessary
</details>

### XDG spec compliance

The Astra CLI will now respect the `$XDG_DATA_HOME` and `$XDG_CONFIG_HOME` environment variables for determining where to store its home folder and `.astrarc` file respectively.

### Misc changes + bug fixes

<details>
  <summary>Other minor changes (not exhaustive)</summary>
  <ul>
    <li>automatically patch cqlsh script to work on machines with a newer python version as default by testing for older python versions explicitly</li>
    <li>consistent support of the `--output` flag across all commands–either a format is supported, or the command will error out before doing anything</li>
    <li>removed `astra-init` script in favor of the `astra compgen` command</li>
    <li>removed `astra login` since it was just an alias of `astra setup`</li>
    <li>`setup` command has been completely rewritten and improved to be much more interactive and user-friendly</li>
    <li>fewer API calls will be made due to better internal caching and logic (commands may be much faster now!)</li>
    <li>fixed inconsistent shell coloring in places (--no-color now definitively works everywhere)</li>
    <li>fixed issues with not being able to use IDs in place of names in some places (e.g. `db delete [id]`)</li>
    <li>timeout durations can now be parsed using iso8601 durations or with simple time units (ms, s, m, h)</li>
    <li>.astrarc parsing is stricter now</li>
    <li>improved .env + .ini parsing + printing</li>
    <li>and more.</li>
  </ul>
</details>

## Troubleshooting

### Common issues

**MacOS security warnings**

If you get an "unidentified developer" error on macOS, see the [macOS warning section](#-macos-warning) above.

**Authentication failures**

If commands fail with authentication errors:
- Verify your token is valid and hasn't expired
- Check that you're using the correct environment (`--env` flag)
- Run `astra config list` to verify your configuration profile settings
- Try running `astra setup` again to reconfigure your credentials

**External programs not working (cqlsh, dsbulk, pulsar-shell)**

If external programs fail to download or execute:
- Check your internet connection
- Verify you have write permissions to your `ASTRA_HOME` directory
- Try removing the cached binary and letting the CLI re-download it:
  ```bash
  # Find the path first
  astra db cqlsh path  # (or dsbulk path, or streaming pulsar path)
  # Then remove the parent directory and try again
  ```

**Finding your configuration files**

If you're unsure where your `.astrarc` file or home folder is located, or if you're getting warnings about multiple paths:
- Run `astra config path` to see where your `.astrarc` file is (or would be) located
- Run `astra config home path` to see where your Astra home folder is located
- If you see warnings about multiple `.astrarc` files or home folders existing in different locations:
  - Remove or migrate the lower priority files/folders (priority: custom env var > XDG spec > default home directory)
  - Or, suppress the warning by setting `ASTRA_IGNORE_MULTIPLE_PATHS=true`

**General debugging**

For any issues:
- Use the `--verbose` flag to see detailed output
- Use the `--dump-logs` flag to save logs to a file for later inspection
- Check the [GitHub issues](https://github.com/datastax/astra-cli/issues) for similar problems
- Open a new issue if your problem isn't already reported
