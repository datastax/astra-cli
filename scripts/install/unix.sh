#!/usr/bin/env sh

set -eu
IFS=$(printf '\n\t')

# Utilities
error() {
  printf "${RED}%s${RESET}\n" "$(printf "$1")" # `printf $1` to preserve newlines
  exit 1
}

checklist() {
  printf "${LIGHT_GRAY}→ %s${RESET}\n" "$(printf "$1")"
}

underline(){
  printf "$(tput smul)%s$(tput rmul)" "$(printf "$1")"
}

tildify() {
  case $1 in "$HOME"*) echo "~${1#"$HOME"}";; *) echo "$1";; esac
}

# Constants
ASTRA_CLI_VERSION="1.0.0-alpha.4"

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

# Colors
if false; then
  LIGHT_GRAY=$(tput setaf 245)
  BLUE=$(tput setaf 110)
  PURPLE=$(tput setaf 134)
  RED=$(tput setaf 167)
  GREEN=$(tput setaf 76)
else
  LIGHT_GRAY=$(tput setaf 7)
  BLUE=$(tput setaf 6)
  PURPLE=$(tput setaf 5)
  RED=$(tput setaf 1)
  GREEN=$(tput setaf 2)
fi

RESET=$(tput sgr0)

# Prelude
echo "$PURPLE"
echo "    _____            __                  "
echo "   /  _  \   _______/  |_____________    "
echo "  /  /_\  \ /  ___/\   __\_  __ \__  \   "
echo " /    |    \\___ \  |  |  |  | \// __ \_ "
echo " \____|__  /____  > |__|  |__|  (____  / "
echo "         \/     \/                   \/  "
echo ""
echo "                    Installer: $ASTRA_CLI_VERSION"
echo "$RESET"

# Required tools check
if ! command -v curl >/dev/null 2>&1; then
  error "Error: curl is not installed. Please install curl and try again."
fi

if ! command -v tar >/dev/null 2>&1; then
  error "Error: tar is not installed. Please install tar and try again."
fi

checklist "Required tools are available."

# Existing installation checks
existing_install_path=$(command -v astra 2>/dev/null || { [ -d "$ASTRA_CLI_DIR" ] && echo "$ASTRA_CLI_DIR"; } || { [ -d "${ASTRA_DIR:-}" ] && echo "$ASTRA_DIR"; } || echo "")

if [ -n "$existing_install_path" ] && [ -f "$existing_install_path/astra" ]; then
  echo ""
  echo "${RED}Error: An existing astra installation was already found.${RESET}"
  echo ""
  echo "An existing installation was found at $(underline "$(tildify "$existing_install_path")")."
  echo ""
  echo "If you want to update the existing installation, please do one of the following:"
  echo "${BLUE}→ ${LIGHT_GRAY}(< astra-cli 1.x)${RESET} Remove the existing installation manually and re-run this installer."
  echo "${BLUE}→ ${LIGHT_GRAY}(> astra-cli 1.x)${RESET} Run ${BLUE}astra upgrade${RESET} to automatically update to the latest version."
  echo "${BLUE}→ ${LIGHT_GRAY}(> astra-cli 1.x)${RESET} Run ${BLUE}astra nuke${RESET} to completely remove the CLI and then re-run this installer."
  echo ""
  exit 1
else
  checklist "No existing installation found."
fi

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

install_url="https://github.com/toptobes/astra-cli-pico/releases/download/v$ASTRA_CLI_VERSION/astra-$os-$arch.tar.gz"

# Verify installation path
echo ""
echo "${GREEN}Ready to install Astra CLI ✅${RESET}"
echo ""
echo "Do you want to install Astra CLI to $(underline "$(tildify "$ASTRA_CLI_DIR/")")? ${BLUE}[Y]es/[d]ifferent path/[c]ancel${RESET}"

while true; do
  printf "%s" "${BLUE}> ${RESET}"
  read -r res

  case ${res:-y} in
    [Yy]* )
      for _ in $(seq 1 5); do tput cuu1 && tput el; done
      break;;
    [Dd]*)
      echo ""
      echo "${RED}To use a custom installation path, please globally set the ASTRA_HOME environment variable.${RESET}"
      echo ""
      echo "This variable $(tput bold)must remain in place forever${RESET} (e.g. in your shell profile like $(underline "~/.bashrc"), $(underline "~/.zshrc"), etc.) so that the CLI can always locate its home directory."
      echo ""
      echo "After setting it, restart your terminal or run 'source ~/.bashrc' (or the appropriate file) to apply the change."
      exit 1
      ;;
    [Cc]* )
      error "\nCancelling installation.";;
    * )
      echo "Please answer ${BLUE}yes${RESET}, ${BLUE}no${RESET}, or ${BLUE}cancel${RESET}.";;
  esac
done

# Create installation directory
if mkdir -p "$ASTRA_CLI_DIR"; then
  checklist "Using installation dir $(underline "$(tildify "$ASTRA_CLI_DIR/")")${RESET}${LIGHT_GRAY}."
else
  error "Failed to create installation directory at $(underline "$(tildify "$ASTRA_CLI_DIR/")")."
fi

# Download and extract
echo ""
echo "Downloading archive..."

EXE_PATH="$ASTRA_CLI_DIR/astra"
TAR_PATH="$ASTRA_CLI_DIR/astra.tar.gz"

if curl -fL --progress-bar "$install_url" > "$TAR_PATH"; then
  for _ in $(seq 1 3); do tput cuu1 && tput el; done
  checklist "Archive downloaded."
else
  rm "$TAR_PATH" 2>/dev/null || true
  error "\nError: Failed to download the archive to $(underline "$(tildify "$TAR_PATH")"). Please check your internet connection, verify write permissions, and try again."
fi

if tar -xzf "$TAR_PATH" -C "$ASTRA_CLI_DIR" && rm "$TAR_PATH"; then
  checklist "Archive verified and extracted."
else
  rm "$TAR_PATH" "$EXE_PATH" 2>/dev/null || true
  error "\nError: Failed to extract the archive at $(underline "$(tildify "$TAR_PATH")"))."
fi

# Postlude
echo ""
echo "${GREEN}Astra CLI installed successfully! 🎉${RESET}"
echo ""
echo "Next steps:"
echo ""

if [ $os = "macos" ] && command -v xattr >/dev/null 2>&1 && xattr -l "$EXE_PATH" | grep -q "com.apple.quarantine"; then
  echo "${LIGHT_GRAY}# Run the following to remove the quarantine label from the binary{RESET}"
  echo "${BLUE}\$${RESET} xattr -d com.apple.quarantine \"$(tildify "$EXE_PATH")\""
  echo ""
fi

echo """${LIGHT_GRAY}# Append the following to your shell profile (e.g. $(underline "~/.bashrc") , $(underline "~/.zshrc"), etc.)${RESET}"""
if [ "$ASTRA_CLI_DIR_RESOLVER" = "custom" ]; then
  echo "${BLUE}\$${RESET} eval \"\$($(tildify "$EXE_PATH") shellenv --home \"$(tildify "$ASTRA_HOME")\")\""
else
  echo "${BLUE}\$${RESET} eval \"\$($(tildify "$EXE_PATH") shellenv)\""
fi
echo ""

echo "${LIGHT_GRAY}# Run the following to get started!${RESET}"
echo "${BLUE}\$${RESET} astra setup"
