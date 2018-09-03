#!/usr/bin/env bash

lokalise --config ./local.lokalise.properties d --type xml --bundle_structure app/src/main/res/values-%LANG_ISO%/strings.xml --unzip_to .