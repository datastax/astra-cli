#!/bin/bash

java -agentlib:native-image-agent=config-output-dir=. -jar target/astra-shell-0.1-alpha4-shaded.jar "$@"

