#!/bin/bash

clear
echo " "
echo "   █████╗ ███████╗████████╗██████╗  █████╗     ███████╗██╗  ██╗███████╗██╗     ██╗"     
echo "  ██╔══██╗██╔════╝╚══██╔══╝██╔══██╗██╔══██╗    ██╔════╝██║  ██║██╔════╝██║     ██║  "
echo "  ███████║███████╗   ██║   ██████╔╝███████║    ███████╗███████║█████╗  ██║     ██║ "  
echo "  ██╔══██║╚════██║   ██║   ██╔══██╗██╔══██║    ╚════██║██╔══██║██╔══╝  ██║     ██║"
echo "  ██║  ██║███████║   ██║   ██║  ██║██║  ██║    ███████║██║  ██║███████╗███████╗███████╗"
echo "  ╚═╝  ╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝    ╚══════╝╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝"
echo " "

ASTRA_CLI_VERSION="0.1.alpha3"

echo "Building Astra Cli"
mvn versions:set -DnewVersion=${ASTRA_CLI_VERSION}
mvn package -Dmaven.test.skip=true
echo "$(tput setaf 2)[OK]$(tput setaf 7) - Jar"

echo "Building autocompletion file"
mvn test -Dtest="com.datastax.astra.bash.GenerateBashCompletion"
chmod 700 ./dist/astra
chmod 700 ./dist/astra-init.sh
echo "$(tput setaf 2)[OK]$(tput setaf 7) - Autocompletion file"

echo "Building Archive"
ASTRA_CLI_RELEASE_FOLDER="/tmp/$ASTRA_CLI_VERSION"
rm -Rf $ASTRA_CLI_RELEASE_FOLDER 
mkdir $ASTRA_CLI_RELEASE_FOLDER
cp ./target/astra-shell-${ASTRA_CLI_VERSION}-shaded.jar ${ASTRA_CLI_RELEASE_FOLDER}/astra-shell.jar
cp ./dist/astra-init.sh ${ASTRA_CLI_RELEASE_FOLDER}
cp ./dist/astra ${ASTRA_CLI_RELEASE_FOLDER}
cd ${ASTRA_CLI_RELEASE_FOLDER}
zip astra-cli-${ASTRA_CLI_VERSION}.zip *
zip -d /tmp/${ASTRA_CLI_VERSION}/astra-cli-${ASTRA_CLI_VERSION}.zip __MACOSX/\* 2>/dev/null
zip -d /tmp/${ASTRA_CLI_VERSION}/astra-cli-${ASTRA_CLI_VERSION}.zip \*/.DS_Store 2>/dev/null
echo "$(tput setaf 2)[OK]$(tput setaf 7) - Archive OK"

echo "Moving Archive"
cp /tmp/${ASTRA_CLI_VERSION}/astra-cli-${ASTRA_CLI_VERSION}.zip /Users/cedricklunven/dev/workspaces/datastax/datastaxdevs.github.io/cli





