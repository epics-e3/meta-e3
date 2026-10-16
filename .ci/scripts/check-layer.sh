#!/bin/sh
# Run yocto-check-layer on meta-e3.
#
# Must run inside "kas shell .ci/kas/base.yml:.ci/kas/check-layer.yml", where
# meta-e3 is checked out but not in BBLAYERS (otherwise the script skips it).
set -eu

layer=$(cd "$(dirname "$0")/../.." && pwd)
# Dependency layers are whatever kas put in BBLAYERS (oe-core's meta).
deps=$(bitbake-getvar --quiet --value BBLAYERS)
log="${BUILDDIR}/yocto-check-layer.log"

echo "Layer under test: ${layer}"
echo "Dependencies: ${deps}"

# --no-auto: test exactly this directory, do not search it for layers (e.g.
# kas checkouts left inside the repository when KAS_WORK_DIR is not set).
# shellcheck disable=SC2086 # deps is a list of paths
yocto-check-layer --no-auto --no-auto-dependency --output-log "${log}" \
    --dependency ${deps} -- "${layer}"

# yocto-check-layer exits 0 when every layer was skipped.
if grep -q "SKIPPED" "${log}"; then
    echo "error: yocto-check-layer skipped the layer, see ${log}" >&2
    exit 1
fi
