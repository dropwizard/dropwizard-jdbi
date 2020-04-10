#!/bin/bash
set -e
set -uxo pipefail

./mvnw -B deploy --settings 'ci/settings.xml' -DskipTests=true
