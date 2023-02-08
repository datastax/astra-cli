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

echo "$(tput setaf 3)[ORGANIZATION]$(tput sgr0)"
echo ""
echo "$(tput setaf 4)astra org$(tput sgr0)"
astra org
echo "$(tput setaf 4)astra org id$(tput sgr0)"
astra org id
echo ""
echo "$(tput setaf 4)astra org name$(tput sgr0)"
astra org name

echo ""
echo "$(tput setaf 3)[CONFIGURATION]$(tput sgr0)"

echo ""
echo "$(tput setaf 4)astra config get default$(tput sgr0)"
astra config get default

echo "$(tput setaf 4)astra config create --token$(tput sgr0)"
astra config create demo --token AstraCS:demWmnCBXelfzmkSzpMcZsTZ:4d8f727da0150457f9cda96801c990fe3e29a68eaecd122c5cd444d07237a793

echo ""
echo "$(tput setaf 4)astra config get demo$(tput sgr0)"
astra config get demo

echo "$(tput setaf 4)astra config list$(tput sgr0)"
astra config list

echo ""
echo "$(tput setaf 3)[DB]$(tput sgr0)"

