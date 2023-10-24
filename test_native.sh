#!/bin/bash

clear
echo " "
echo "    _____            __ "
echo "   /  _  \   _______/  |_____________ "
echo "  /  /_\  \ /  ___/\   __\_  __ \__  \ "
echo " /    |    \\\___ \  |  |  |  | \// __ \_ "
echo " \____|__  /____  > |__|  |__|  (____  / "
echo "         \/     \/                   \/ "
echo " "

runCommand () {
  echo "$(tput setaf 3)- $1$(tput sgr0)"
  target/astra-native $1
}

echo "$(tput setaf 4)[ORGANIZATION]$(tput sgr0)"
runCommand "org"
runCommand "org -o csv"
runCommand "org -o json"
runCommand "org -v"
runCommand "org id"
runCommand "org name"

echo "$(tput setaf 4)[CONFIGURATION]$(tput sgr0)"
runCommand "config list"
runCommand "config create demo --token $ASTRA_DB_APPLICATION_TOKEN"
runCommand "config get demo"
runCommand "config describe demo"
runCommand "config use demo"
runCommand "config use cedrick.lunven@datastax.com"
runCommand "config delete demo"

echo "$(tput setaf 4)[ROLES]$(tput sgr0)"
runCommand "role list"
runCommand "role describe ad0566b5-2a67-49de-89e8-92258c2f2c98"
runCommand "role get ad0566b5-2a67-49de-89e8-92258c2f2c98"

echo "$(tput setaf 4)[USERS]$(tput sgr0)"
runCommand "user list"
runCommand "user list --no-color"
runCommand "user get cedrick.lunven@datastax.com"
runCommand "user invite celphys@gmail.com"
runCommand "user get celphys@gmail.com"
runCommand "user delete celphys@gmail.com"

echo "$(tput setaf 4)[TOKENS]$(tput sgr0)"
runCommand "token list"
runCommand "token list --no-color"
runCommand "token get cedrick.lunven@datastax.com"
runCommand "user invite celphys@gmail.com"
runCommand "user get celphys@gmail.com"
runCommand "user delete celphys@gmail.com"

echo "$(tput setaf 4)[TOKENS]$(tput sgr0)"
export ROLE_ADMIN=`astra role list | grep Organization | cut -b 3-38`
runCommand 'token create -r $ROLE_ADMIN'

    @Group(
       name= "token",
       description = "Manage tokens",
       defaultCommand = TokenGetCmd.class,
       commands = {
         TokenListCmd.class, TokenGetCmd.class, TokenCreateCmd.class, TokenDeleteCmd.class, TokenRevokeCmd.class
     })


