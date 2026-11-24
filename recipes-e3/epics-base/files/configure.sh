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

# Replace the per-recipe  --sysroot path so the installed config
# file resolves $(SYSROOT) to each recipe's own staging dir
SYSROOT_CC=$(echo "${CC}" | sed 's# --sysroot=[^ ]*# --sysroot=$(SYSROOT)#g')
SYSROOT_CXX=$(echo "${CXX}" | sed 's# --sysroot=[^ ]*# --sysroot=$(SYSROOT)#g')
SYSROOT_LD=$(echo "${LD}" | sed 's# --sysroot=[^ ]*# --sysroot=$(SYSROOT)#g')

cat <<EOF >>configure/os/CONFIG_SITE.linux-x86_64.${EPICS_TARGET_ARCH}
CC=${SYSROOT_CC}
CCC=${SYSROOT_CXX}
CMD_CFLAGS=\$(DEBUG_PREFIX_MAP)
CMD_CPPFLAGS=\$(DEBUG_PREFIX_MAP)
CMD_LDFLAGS=\$(DEBUG_PREFIX_MAP)
AR=${AR} -rc
RANLIB=${RANLIB}
LD=${SYSROOT_LD}
LDFLAGS=${LDFLAGS}
EOF
