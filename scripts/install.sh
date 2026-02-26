#!/usr/bin/env sh

set -eu
IFS=$(printf '\n\t')

exists() {
  command -v "$1" >/dev/null 2>&1
}

COLORS_SUPPORTED=false
if exists tput && [ "$(tput colors 2>/dev/null || echo 0)" -ge 8 ]; then
  COLORS_SUPPORTED=true
fi

color() {
  if [ "$COLORS_SUPPORTED" = true ]; then
    tput "$@"
  else
    printf ""
  fi
}

error() {
  printf "${RED}%s${RESET}\n" "$(printf "$1")" # `printf $1` to preserve newlines
  exit 1
}

checklist() {
  printf "${LIGHT_GRAY}→ %s${RESET}\n" "$(printf "$1")"
}

underline(){
  printf "$(color smul)%s$(color rmul)" "$(printf "$1")"
}

tildify() {
  case $1 in "$HOME"*) echo "~${1#"$HOME"}";; *) echo "$1";; esac
}

renderComment() {
  printf "${LIGHT_GRAY}# %s${RESET}\n" "$1"
}

renderCommand() {
  printf "${BLUE}\$${RESET} %s\n" "$1"
}

# Constants
ASTRA_CLI_VERSION="1.0.4"

# Checksum constants (updated automatically by CI)
LINUX_X86_64_CHECKSUM="f9b70a89cd510c042f47272ab08c126c2556ed469def22967d812ebdab655cc9"
LINUX_ARM64_CHECKSUM="09d20891604e85eec7ea1d9f7e3e4263c02080a74cd40d754f81adb00df3a97e"
MACOS_X86_64_CHECKSUM="995486b5337926b0a030975013677bf77d88c91ea4bba60cf13d13ff4ed34616"
MACOS_ARM64_CHECKSUM="76c845ffd56fe50f096a9cd2e91c0a37228ceac4850fade2d663b0d6215bf950"

if [ -n "${ASTRA_HOME:-}" ]; then
  ASTRA_CLI_DIR_RESOLVER="custom"
  ASTRA_CLI_DIR="$ASTRA_HOME/cli"
elif [ -n "${XDG_DATA_HOME:-}" ]; then
  ASTRA_CLI_DIR_RESOLVER="xdg"
  ASTRA_CLI_DIR="$XDG_DATA_HOME/astra/cli"
else
  ASTRA_CLI_DIR_RESOLVER="home"
  ASTRA_CLI_DIR="$HOME/.astra/cli"
fi

EXE_PATH="$ASTRA_CLI_DIR/astra"
TAR_PATH="$ASTRA_CLI_DIR/astra.tar.gz"

# Colors
if [ -n "$(color colors)" ] && [ "$(color colors)" -ge 256 ]; then
  LIGHT_GRAY=$(color setaf 245)
  BLUE=$(color setaf 110)
  PURPLE=$(color setaf 134)
  RED=$(color setaf 167)
  GREEN=$(color setaf 76)
else
  LIGHT_GRAY=$(color setaf 7)
  BLUE=$(color setaf 6)
  PURPLE=$(color setaf 5)
  RED=$(color setaf 1)
  GREEN=$(color setaf 2)
fi

RESET=$(color sgr0)

# Prelude
echo "$PURPLE"
echo "    _____            __                   "
echo "   /  _  \   _______/  |_____________     "
echo "  /  /_\  \ /  ___/\   __\_  __ \__  \    "
echo " /    |    \\___  \  |  |  |  | \// __ \_ "
echo " \____|__  /____  > |__|  |__|  (____  /  "
echo "         \/     \/                   \/   "
echo ""
echo "                    Installer: $ASTRA_CLI_VERSION"
echo "$RESET"

# Required tools check
if ! exists curl; then
  error "Error: curl is not installed. Please install curl and try again."
fi

if ! exists tar; then
  error "Error: tar is not installed. Please install tar and try again."
fi

if ! exists sha256sum && ! exists shasum && ! exists openssl; then
  error "Error: No SHA-256 checksum tool found. Please install sha256sum, shasum, or openssl and try again."
fi

checklist "Required tools are available."

# Make installation URL
case $(uname) in
  Linux*|SunOS*|FreeBSD*)
    os="linux";;
  Darwin*)
    os="macos";;
  *)
    error "\nError: Unsupported OS. This installation script supports only Linux and macOS.";;
esac

case $(uname -m) in
  x86_64*|amd64*)
    arch="x86_64";;
  arm64*|aarch64*)
    arch="arm64";;
  *)
    error "\nError: Unsupported architecture. This installation script supports only x86_64 and arm64 architectures.";;
esac

install_url="https://github.com/datastax/astra-cli/releases/download/v$ASTRA_CLI_VERSION/astra-$os-$arch.tar.gz"

# Out of order but shh
print_next_steps() {
  steps_str=$(mk_print_next_steps_str "$1")

  if [ "$COLORS_SUPPORTED" = true ]; then
    draw_box_around "$steps_str"
  else
    printf '%s\n' "$steps_str"
  fi
}

draw_box_around() {
  max_width=0
  while IFS= read -r line; do
    visible_len=$(visible_len "$line")

    if [ "$visible_len" -gt "$max_width" ]; then
      max_width=$visible_len
    fi
  done <<EOF
$1
EOF

  box_width=$((max_width + 6))
  border_len=$((box_width - 2))

  draw_line "$border_len" "┌" "─" "┐"
  draw_line "$border_len" "│" " " "│"

  while IFS= read -r line; do
    padding=$((max_width - $(visible_len "$line")))
    draw_line "$padding" "${BLUE}│${RESET}  ${line}" " " "  ${BLUE}│${RESET}"
  done <<EOF
$1
EOF

  draw_line "$border_len" "│" " " "│"
  draw_line "$border_len" "└" "─" "┘"
}

# https://stackoverflow.com/a/56170835
visible_len() {
  esc=$(printf '\033')
  stripped=$(printf '%s' "$1" | sed "s/${esc}[^m]*m//g")
  printf '%d' "${#stripped}"
}

draw_line() {
  i=0
  printf '%s' "${BLUE}${2}"
  while [ "$i" -lt "$1" ]; do
    printf "$3"
    i=$((i + 1))
  done
  printf '%s\n' "${4}${RESET}"
}

mk_print_next_steps_str() {
  echo "$1"
  echo ""

  if [ $os = "macos" ] && exists xattr && xattr -l "$EXE_PATH" | grep -q "com.apple.quarantine"; then
    renderComment "Run the following to remove the quarantine label from the binary"
    renderCommand "xattr -d com.apple.quarantine \"$(tildify "$EXE_PATH")\""
    echo ""
  fi

  case "${SHELL:-}" in
    */bash*)
      if [ "$os" = linux ]; then
        print_append_to_shell_profile "${HOME}/.bashrc"
      else
        print_append_to_shell_profile "${HOME}/.bash_profile"
      fi
      ;;
    */zsh*)
      if [ "$os" = linux ]; then
        print_append_to_shell_profile "${ZDOTDIR:-"${HOME}"}/.zshrc"
      else
        print_append_to_shell_profile "${ZDOTDIR:-"${HOME}"}/.zprofile"
      fi
      ;;
    *)
      renderComment "Add astra to your PATH in your shell profile"
      renderCommand "export PATH=$(dirname \""$(tildify "$EXE_PATH")"\"):\$PATH\""
      echo ""
      ;;
  esac

  renderComment "Run the following to get started!"
  renderCommand "astra setup"
}

print_append_to_shell_profile() {
  file="$1"

  # shellcheck disable=SC2016
  if [ "$ASTRA_CLI_DIR_RESOLVER" = "custom" ]; then
     command="eval \"\$($(tildify "$EXE_PATH") shellenv --home \"$(tildify "$ASTRA_HOME")\")\""
  else
     command="eval \"\$($(tildify "$EXE_PATH") shellenv)\""
  fi

  if [ -w "$file" ]; then
    renderComment "Run the following to enable completions and update your PATH"
    renderCommand "echo '$command' >> $(tildify "$file")"
  else
    renderComment "Append the following to your $(underline "$(tildify "$file")") to enable completions and update your PATH"
    renderCommand "$command"
  fi

  echo ""
}

# Resolve any existing installations
if exists brew; then
  brew_prefix=$(brew --prefix 2>/dev/null || echo "")

  if [ -n "$brew_prefix" ] && [ -f "$brew_prefix/bin/astra" ]; then
    existing_install_path="$brew_prefix/bin/astra"
    existing_package_manager="homebrew"
    return
  fi
fi

if exists astra; then
  existing_install_path=$(command -v astra)
elif [ -f "$ASTRA_CLI_DIR/astra" ]; then
  existing_install_path="$ASTRA_CLI_DIR/astra"
elif [ -f "${ASTRA_DIR:-}/astra" ]; then
  existing_install_path="$ASTRA_DIR/astra"
fi

print_basic_0_x_removal_instructions() {
  echo "To remove the existing Astra CLI binary (but preserve other Astra files), please do the following:"
  echo "${BLUE}→ ${LIGHT_GRAY}rm -f $(tildify "$existing_install_path")${RESET}"
}

print_brew_0_x_removal_instructions() {
  echo "To remove the existing Astra CLI binary (but preserve other Astra files), please do the following:"
  echo "${BLUE}→ ${LIGHT_GRAY}brew uninstall datastax/astra-cli/astra-cli${RESET}"
}

print_basic_1_x_removal_instructions() {
  echo "Prefer to use ${BLUE}astra upgrade${RESET} to automatically update to the latest version."
  echo ""
  echo "Otherwise, to remove the existing Astra CLI binary (but preserve other Astra files), please do the following:"
  echo "${BLUE}→ ${LIGHT_GRAY}rm -f $(tildify "$existing_install_path")${RESET}"
}

print_brew_1_x_removal_instructions() {
  echo "To remove the existing Astra CLI binary (but preserve other Astra files), please do the following:"
  echo "${BLUE}→ ${LIGHT_GRAY}brew uninstall astra${RESET}"
}

print_generic_removal_instructions() {
  echo "If you want to update the existing installation, please do one of the following:"
  echo "${BLUE}→ ${LIGHT_GRAY}(< astra-cli 1.x)${RESET} Remove the existing installation manually and re-run this installer."
  echo "${BLUE}→ ${LIGHT_GRAY}(> astra-cli 1.x)${RESET} Run ${BLUE}astra upgrade${RESET} to automatically update to the latest version."
  echo "${BLUE}→ ${LIGHT_GRAY}(> astra-cli 1.x)${RESET} Run ${BLUE}astra nuke${RESET} to completely remove the CLI and then re-run this installer."
}

if [ -f "${existing_install_path:-}" ]; then
  echo ""
  echo "${RED}Error: An existing astra installation was already found.${RESET}"
  echo ""
  echo "An existing installation was found at $(underline "$(tildify "$existing_install_path")")."
  echo ""

  case "$("$existing_install_path" --version 2>/dev/null | cut -d. -f1)" in
    *0)
      if [ "${existing_package_manager:-}" = "homebrew" ]; then
        print_brew_0_x_removal_instructions
      else
        print_basic_0_x_removal_instructions
      fi
      ;;
    *1)
      if [ "${existing_package_manager:-}" = "homebrew" ]; then
        print_brew_1_x_removal_instructions
      else
        print_basic_1_x_removal_instructions
      fi
      ;;
    *)
      print_generic_removal_instructions
      ;;
  esac

  if ! exists astra; then
    echo ""
    print_next_steps "If you just can't use ${BLUE}astra${RESET}, ensure you've done the following:"
  fi

  exit 1
else
  checklist "No existing installation found."
fi

# Create installation directory
if mkdir -p "$ASTRA_CLI_DIR"; then
  checklist "Using installation dir $(underline "$(tildify "$ASTRA_CLI_DIR/")")${RESET}${LIGHT_GRAY}."
else
  error "Failed to create installation directory at $(underline "$(tildify "$ASTRA_CLI_DIR/")")."
fi

# Download and extract
echo ""
echo "Downloading archive..."

if curl -fL --progress-bar "$install_url" > "$TAR_PATH"; then
  for _ in $(seq 1 3); do color cuu1 && color el; done
  checklist "Archive downloaded."
else
  rm "$TAR_PATH" 2>/dev/null || true
  error "\nError: Failed to download the archive to $(underline "$(tildify "$TAR_PATH")"). Please check your internet connection, verify write permissions, and try again."
fi

# Verify checksum
case "$os-$arch" in
  linux-x86_64)
    expected_checksum="$LINUX_X86_64_CHECKSUM"
    ;;
  linux-arm64)
    expected_checksum="$LINUX_ARM64_CHECKSUM"
    ;;
  macos-x86_64)
    expected_checksum="$MACOS_X86_64_CHECKSUM"
    ;;
  macos-arm64)
    expected_checksum="$MACOS_ARM64_CHECKSUM"
    ;;
esac

if exists sha256sum; then
  actual_checksum=$(sha256sum "$TAR_PATH" | cut -d' ' -f1)
elif exists shasum; then
  actual_checksum=$(shasum -a 256 "$TAR_PATH" | awk '{print $1}')
else
  actual_checksum=$(openssl dgst -sha256 "$TAR_PATH" | awk '{print $2}')
fi

if [ "$actual_checksum" != "$expected_checksum" ]; then
  rm "$TAR_PATH" 2>/dev/null || true
  error "\nError: Checksum verification failed. Expected $expected_checksum but got $actual_checksum. The downloaded file may be corrupted."
fi

checklist "Checksum verified."

if tar -xzf "$TAR_PATH" -C "$ASTRA_CLI_DIR" --strip-components=2 --no-same-owner astra/bin/astra; then
  checklist "Archive verified and extracted."
else
  rm "$TAR_PATH" "$EXE_PATH" 2>/dev/null || true
  error "\nError: Failed to extract the archive at $(underline "$(tildify "$TAR_PATH")"))."
fi

rm "$TAR_PATH" 2>/dev/null || true

# Postlude
echo ""
echo "${GREEN}Astra CLI installed successfully! 🎉${RESET}"
echo ""
print_next_steps "Next steps:"
