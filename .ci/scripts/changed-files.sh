#!/bin/sh
# Print the files changed between a base ref and HEAD, one per line,
# relative to the repository root. Deleted files are omitted.
#
# Usage: changed-files.sh <base-ref>     e.g. changed-files.sh origin/main
set -eu

base=${1:?usage: $0 <base-ref>}
cd "$(dirname "$0")/../.."
git diff --name-only --diff-filter=d "${base}...HEAD"
