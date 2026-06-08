# EPICS needs perl for building. Also it needs to find the perl libraries from
# EPICS BASE native installation, so we define PERL5LIB.
inherit perlnative
export PERL5LIB = "${STAGING_LIBDIR_NATIVE}/perl"

DEPENDS += "epics-base-native epics-base chrpath-native"

# HOST and TARGET architecture names are different for EPICS so this
# translate and create appropriate variables.
def _epics_arch(arch_str):
    arch_map = {
        'powerpc64': 'linux-ppc64',
        'x86_64':    'linux-x86_64',
    }
    return arch_map.get(arch_str, 'linux-' + arch_str)

EPICS_TARGET_ARCH = "${@_epics_arch(d.getVar('TARGET_ARCH'))}"
EPICS_HOST_ARCH   = "${@_epics_arch(d.getVar('BUILD_ARCH'))}"
export EPICS_HOST_ARCH

EPICS_BASE = "${STAGING_DIR_HOST}/epics"
export EPICS_BASE

do_configure:append() {
    mkdir -p ${STAGING_DIR_HOST}/epics/bin
    ln -sfn ${STAGING_BINDIR_NATIVE} ${STAGING_DIR_HOST}/epics/bin/${EPICS_HOST_ARCH}

    echo "EPICS_BASE=${EPICS_BASE}" >> ${S}/configure/RELEASE.local
    if [ "${PN}" != "pvxs" ]; then
        echo "PVXS:=${STAGING_DIR_HOST}/pvxs" >> ${S}/configure/RELEASE.local
    fi

    cat > ${S}/configure/CONFIG_SITE.local <<EOF
COMMANDLINE_LIBRARY=EPICS
BUILD_ARCHS=${EPICS_TARGET_ARCH}
EPICS_HOST_BIN=${STAGING_BINDIR_NATIVE}
EPICS_BASE_HOST_BIN=${STAGING_BINDIR_NATIVE}
TOOLS=${STAGING_BINDIR_NATIVE}
SYSROOT=${STAGING_DIR_HOST}
OP_SYS_INCLUDES += -I${STAGING_INCDIR}
OP_SYS_LDFLAGS += -L${STAGING_LIBDIR}
FILTERMAKEFLAGS = \$(PERL) \$(TOOLS)/filterMakeflags.pl
EOF
}
