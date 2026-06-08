#!/bin/bash

EPICS_BASE="${S}/epics"

case "${TARGET_ARCH}" in
powerpc64) EPICS_TARGET_ARCH="linux-ppc64" ;;
x86_64) EPICS_TARGET_ARCH="linux-x86_64" ;;
*) EPICS_TARGET_ARCH="linux-${TARGET_ARCH}" ;;
esac

cat <<EOF >>configure/CONFIG_SITE.local
INSTALL_LOCATION=${EPICS_BASE}
EPICS_HOST_BIN=${STAGING_BINDIR_NATIVE}
TOOLS=${STAGING_BINDIR_NATIVE}
BUILD_ARCHS=${EPICS_TARGET_ARCH}
EOF

cat <<EOF >>configure/os/CONFIG_SITE.Common.linuxCommon
POSIX_CFLAGS += -std=gnu17
# --disable-new-dtags prevents LD_LIBRARY_PATH from overriding RPATH.
# -rpath is the runtime path baked into the binary (no sysroot prefix needed).
# -rpath-link is the link-time search path; use \$(SYSROOT) so it resolves
# correctly for every recipe that consumes this config from its own sysroot.
OP_SYS_LDFLAGS += -Wl,--disable-new-dtags -Wl,-rpath,${libdir} -Wl,-rpath,/epics/lib/\$(EPICS_HOST_ARCH) -Wl,-rpath-link,\$(SYSROOT)${libdir} -Wl,-rpath-link,\$(SYSROOT)/epics/lib/\$(EPICS_HOST_ARCH) -L\$(SYSROOT)${libdir}
EOF

cat <<EOF >>configure/os/CONFIG_SITE.linux-x86_64.${EPICS_TARGET_ARCH}
# CC, AR, RANLIB, etc. come from the environment via make -e.
# EPICS uses CCC for the C++ compiler; bridge it from the standard CXX env var.
CCC=\$(CXX)
EOF
