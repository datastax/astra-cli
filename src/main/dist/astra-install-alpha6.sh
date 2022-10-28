#!/bin/bash

set -e

track_last_command() {
    last_command=$current_command
    current_command=$BASH_COMMAND
}
trap track_last_command DEBUG

echo_failed_command() {
    local exit_code="$?"
	if [[ "$exit_code" != "0" ]]; then
		echo "'$last_command': command failed with exit code $exit_code."
	fi
}
trap echo_failed_command EXIT

clear
echo " "
echo "    _____            __ "
echo "   /  _  \   _______/  |_____________ "
echo "  /  /_\  \ /  ___/\   __\_  __ \__  \ "
echo " /    |    \\___ \  |  |  |  | \// __ \_ "
echo " \____|__  /____  > |__|  |__|  (____  / "
echo "         \/     \/                   \/ "
echo " "

# Global variables
ASTRA_CLI_VERSION="0.1.alpha6"

echo "Installing Astra Cli $ASTRA_CLI_VERSION, please wait...      "

ASTRA_CLI_PLATFORM=$(uname)
ASTRA_CLI_DIR="$HOME/.astra/cli"

# Local variables
astra_tmp_folder="$HOME/.astra/tmp"
astra_scb_folder="$HOME/.astra/scb"
astra_zip_file="${astra_tmp_folder}/astra-cli-${ASTRA_CLI_VERSION}.zip"
astra_zip_base_folder="${astra_tmp_folder}/astra-cli-${ASTRA_CLI_VERSION}"

# Config Files
astra_bash_profile="${HOME}/.bash_profile"
astra_profile="${HOME}/.profile"
astra_bashrc="${HOME}/.bashrc"
astra_zshrc="${ZDOTDIR:-${HOME}}/.zshrc"
astra_init_snippet=$( cat << EOF
#THIS MUST BE AT THE END OF THE FILE FOR ASTRA_CLI TO WORK!!!
export ASTRADIR="$ASTRA_CLI_DIR"
[[ -s "${ASTRA_CLI_DIR}/astra-cli-autocomplete.sh" ]] && source "${ASTRA_CLI_DIR}/astra-cli-autocomplete.sh"
EOF
)

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
solaris=false;
freebsd=false;
case "$(uname)" in
    CYGWIN*)
        cygwin=true
        ;;
    Darwin*)
        darwin=true
        ;;
    SunOS*)
        solaris=true
        ;;
    FreeBSD*)
        freebsd=true
esac

# Sanity checks
echo ""
echo "Checking prerequisites:"

if [ -d "$ASTRA_DIR" ]; then
	echo ""
	echo "======================================================================================================"
	echo " You already have ASTRA-CLI installed, deleting..."
	echo "======================================================================================================"
	echo ""
	rm -Rf ${ASTRA_CLI_DIR}
	exit 0
fi
echo "$(tput setaf 2)[OK]$(tput setaf 7) - Ready to install."

if ! command -v unzip > /dev/null; then
	echo "Not found."
	echo "======================================================================================================"
	echo " Please install unzip on your system using your favourite package manager."
	echo ""
	echo " Restart after installing unzip."
	echo "======================================================================================================"
	echo ""
	exit 1
fi
echo "$(tput setaf 2)[OK]$(tput setaf 7) - unzip command is available"

if ! command -v zip > /dev/null; then
	echo "Not found."
	echo "======================================================================================================"
	echo " Please install zip on your system using your favourite package manager."
	echo ""
	echo " Restart after installing zip."
	echo "======================================================================================================"
	echo ""
	exit 1
fi
echo "$(tput setaf 2)[OK]$(tput setaf 7) - zip command is available"

if ! command -v curl > /dev/null; then
	echo "Not found."
	echo ""
	echo "======================================================================================================"
	echo " Please install curl on your system using your favourite package manager."
	echo ""
	echo " Restart after installing curl."
	echo "======================================================================================================"
	echo ""
	exit 1
fi
echo "$(tput setaf 2)[OK]$(tput setaf 7) - curl command is available"

echo ""
echo "Preparing directories:"
mkdir -p "$astra_tmp_folder"
echo "$(tput setaf 2)[OK]$(tput setaf 7) - Created $astra_tmp_folder"
mkdir -p "$ASTRA_CLI_DIR"
echo "$(tput setaf 2)[OK]$(tput setaf 7) - Created $ASTRA_CLI_DIR"
mkdir -p "$astra_scb_folder"
echo "$(tput setaf 2)[OK]$(tput setaf 7) - Created $astra_scb_folder"

echo ""
echo "Downloading archive:"
download_url="https://github.com/datastax/astra-cli/releases/download/${ASTRA_CLI_VERSION}/astra-cli-${ASTRA_CLI_VERSION}.zip"
astra_zip_file="${astra_tmp_folder}/astra-cli-${ASTRA_CLI_VERSION}.zip"
if [ -f "$astra_zip_file" ]; then
	echo "$(tput setaf 2)[OK]$(tput setaf 7) - Archive is already there"
else
	curl --fail --location --progress-bar "$download_url" > "$astra_zip_file"
	echo "$(tput setaf 2)[OK]$(tput setaf 7) - File downloaded"
fi

# check integrity
ARCHIVE_OK=$(unzip -qt "$astra_zip_file" | grep 'No errors detected in compressed data')
if [[ -z "$ARCHIVE_OK" ]]; then
	echo "Downloaded zip archive corrupt. Are you connected to the internet?"
	echo ""
	echo "If problems persist, please ask for help on our Slack:"
	echo "* easy sign up: https://slack.sdkman.io/"
	echo "* report on channel: https://sdkman.slack.com/app_redirect?channel=user-issues"
	exit
fi
echo "$(tput setaf 2)[OK]$(tput setaf 7) - Integrity of the archive checked"


if [[ "$cygwin" == 'true' ]]; then
	astra_tmp_folder=$(cygpath -w "$astra_tmp_folder")
	astra_zip_file=$(cygpath -w "$astra_zip_file")
fi

echo ""
echo "Extracting and installation:"
unzip -qo "$astra_zip_file" -d "$astra_tmp_folder"
echo "$(tput setaf 2)[OK]$(tput setaf 7) - Extraction is successful"

rm -rf "$astra_zip_file"
cp -rf "${astra_tmp_folder}/"* "$ASTRA_CLI_DIR"
echo "$(tput setaf 2)[OK]$(tput setaf 7) - File moved to $ASTRA_CLI_DIR"

rm -rf "${astra_tmp_folder}"
echo "$(tput setaf 2)[OK]$(tput setaf 7) - Installation cleaned up"

if [[ $darwin == true ]]; then
  # Adding on MAC OS
  touch "$astra_bash_profile"
  if [[ -z $(grep 'astra-cli-autocomplete.sh' "$astra_bash_profile") ]]; then
    echo -e "\n$astra_init_snippet" >> "$astra_bash_profile"
    echo "$(tput setaf 2)[OK]$(tput setaf 7) - astra added to ${astra_bash_profile}"
  fi
else
  # Attempt update of interactive bash profile on regular UNIX
  touch "${astra_bashrc}"
  if [[ -z $(grep 'astra-cli-autocomplete.sh' "$astra_bashrc") ]]; then
      echo -e "\n$astra_init_snippet" >> "$astra_bashrc"
      echo "$(tput setaf 2)[OK]$(tput setaf 7) - astra added to ${astra_bashrc}"
  fi
fi

touch "$astra_zshrc"
if [[ -z $(grep 'astra-cli-autocomplete.sh' "$astra_zshrc") ]]; then
    echo -e "\n$astra_init_snippet" >> "$astra_zshrc"
    echo "$(tput setaf 2)[OK]$(tput setaf 7) - astra added to ${astra_zshrc}"
fi

echo "$(tput setaf 2)[OK]$(tput setaf 7) - Installation Successful"
echo ""
echo "Open $(tput setaf 2)A NEW TERMINAL$(tput setaf 7) and run: $(tput setaf 3)astra setup$(tput setaf 7)"
echo ""
echo "You can close this window."