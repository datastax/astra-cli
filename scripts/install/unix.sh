#!/usr/bin/env sh

set -eu
IFS=$(printf '\n\t')

# Constants
ASTRA_CLI_VERSION="1.0.0-alpha.2"

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

# Utilities
error() {
  printf "%s%s%s\n" "$RED" "$(printf "$1")" "$RESET" # `printf $1` to preserve newlines
  exit 1
}

checklist() {
  printf "%s→ %s %s\n" "$LIGHT_GRAY" "$(printf "$1")" "$RESET"
}

# Colors
if [ "$(tput colors)" -ge 256 ]; then
  LIGHT_GRAY=$(tput setaf 245)
  BLUE=$(tput setaf 110)
  PURPLE=$(tput setaf 134)
  RED=$(tput setaf 167)
  GREEN=$(tput setaf 76)
  YELLOW=$(tput setaf 226)
else
  LIGHT_GRAY=$(tput setaf 7)
  BLUE=$(tput setaf 4)
  PURPLE=$(tput setaf 5)
  RED=$(tput setaf 1)
  GREEN=$(tput setaf 2)
  YELLOW=$(tput setaf 3)
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

if [ -n "$existing_install_path" ] && { [ -f "$existing_install_path" ] || { [ -d "$existing_install_path" ] && [ "$(ls "$existing_install_path")" ]; }; }; then
  echo ""
  echo "${RED}Error: An existing astra installation was already found.${RESET}"
  echo ""
  echo "An existing installation was found at $(tput smul)${existing_install_path}$(tput rmul)."
  echo ""
  echo "If you want to update the existing installation, please do one of the following:"
  echo "${BLUE}→ ${LIGHT_GRAY}(< astra-cli 1.x)${RESET} Remove the existing installation manually and re-run this installer."
  echo "${BLUE}→ ${LIGHT_GRAY}(> astra-cli 1.x)${RESET} Run ${BLUE}astra update${RESET} to automatically update to the latest version."
  echo "${BLUE}→ ${LIGHT_GRAY}(> astra-cli 1.x)${RESET} Run ${BLUE}astra nuke${RESET} to completely remove the CLI and then re-run this installer."
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
echo "Do you want to install Astra CLI to $(tput smul)${ASTRA_CLI_DIR}$(tput rmul)? ${BLUE}[Y]es/[d]ifferent path/[c]ancel${RESET}"

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
      echo "This variable $(tput bold)must remain in place forever$(tput sgr0) (e.g. in your shell profile like $(tput smul)~/.bashrc${RESET}, $(tput smul)~/.zshrc${RESET}, etc.) so that the CLI can always locate its home directory."
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
  checklist "Using installation dir $(tput smul)${ASTRA_CLI_DIR}${RESET}${LIGHT_GRAY}."
else
  error "Failed to create installation directory at $(tput smul)${ASTRA_CLI_DIR}."
fi

# Download and extract
echo ""
echo "Downloading archive..."

if curl -fL --progress-bar "$install_url" > "$ASTRA_CLI_DIR/astra.tar.gz"; then
  for _ in $(seq 1 3); do tput cuu1 && tput el; done
  checklist "Archive downloaded."
else
  rm "$ASTRA_CLI_DIR/astra.tar.gz" 2>/dev/null || true
  error "\nError: Failed to download the archive to $(tput smul)${ASTRA_CLI_DIR}$(tput rmul). Please check your internet connection, verify write permissions, and try again."
fi

if tar -xzf "$ASTRA_CLI_DIR/astra.tar.gz" -C "$ASTRA_CLI_DIR" && rm "$ASTRA_CLI_DIR/astra.tar.gz"; then
  checklist "Archive verified and extracted."
else
  rm "$ASTRA_CLI_DIR/astra.tar.gz" "$ASTRA_CLI_DIR/astra" 2>/dev/null || true
  error "\nError: Failed to extract the archive at $(tput smul)${ASTRA_CLI_DIR}$(tput rmul)."
fi

# Add to path
# TODO

# Postlude
echo ""
echo "${GREEN}Astra CLI installed successfully! 🎉${RESET}"
echo ""
echo "Next steps:"
echo ""

if [ $os = "macos" ] && command -v xattr >/dev/null 2>&1 && xattr -l "$ASTRA_CLI_DIR/astra" | grep -q "com.apple.quarantine"; then
  echo "${LIGHT_GRAY}# Run the following to remove the quarantine label from the binary{RESET}"
  echo "${BLUE}\$xattr -d com.apple.quarantine \"$ASTRA_CLI_DIR/astra\""
  echo ""
fi

echo "${LIGHT_GRAY}# Add the following to your shell profile (e.g. $(tput smul)~/.bashrc$(tput rmul), $(tput smul)~/.zshrc$(tput rmul), etc.)${RESET}"
echo "${BLUE}\$${RESET} eval \"\$(${ASTRA_CLI_DIR}/astra shellenv)\""
echo ""

echo "${LIGHT_GRAY}# Run the following to get started!${RESET}"
echo "${BLUE}\$${RESET} astra setup"
