#!/usr/bin/env bash

set -o nounset

api_server="http://localhost:4567"


curl -s ${api_server}/configurations
