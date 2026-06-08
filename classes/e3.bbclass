inherit epics-module
DEPENDS += "require"

# This is a conviniance, as every module will install these files.
FILES:${PN} += "/epics-modules/${E3_MODULE_NAME}"
FILES:${PN} += "/include /lib"
FILES:${PN} += "/include/*.h /lib/*.so"
FILES:${PN}-dev:remove = "/lib/*.so"
INSANE_SKIP:${PN} += "libdir"
INSANE_SKIP:${PN}-dbg += "libdir"


# EPICS version is needed for require in a environment variable.
# The yocto build environment does not work as the conda environment,
# so we need to create the variables here 
def _epics_base_version(d):
    import os
    f = os.path.join(d.getVar('STAGING_DIR_HOST'), 'epics', 'EPICS_BASE_VERSION')
    return open(f).read().strip() if os.path.exists(f) else ''

EPICS_BASE_VERSION = "${@_epics_base_version(d)}"
export EPICS_BASE_VERSION

EPICS_MODULES        = "${STAGING_DIR_HOST}/epics-modules"
export EPICS_MODULES
E3_REQUIRE_LOCATION  = "${EPICS_MODULES}/require"
export E3_REQUIRE_LOCATION
E3_REQUIRE_TOOLS = "${E3_REQUIRE_LOCATION}/share"
export E3_REQUIRE_TOOLS

EPICS_TARGET_ARCH = "${@_epics_arch(d.getVar('TARGET_ARCH'))}"

do_compile() {
    # This is kind of a workaround, EPICS configuration system was troublesome to define
    # specific variables. Doing it here will ensure they are not overwritten later.
    make ${PARALLEL_MAKE} SYSROOT=${STAGING_DIR_HOST} \
        CC="${CC}" \
        CCC="${CXX}" \
        AR="${AR} -rc" \
        RANLIB="${RANLIB}" \
        LD="${LD}" \
        LDFLAGS="${LDFLAGS}" \
        BUILD_ARCHS=${EPICS_TARGET_ARCH} \
        E3_REQUIRE_TOOLS="${STAGING_DIR_HOST}/epics-modules/require/share" \
        EPICS_MODULES="${STAGING_DIR_HOST}/epics-modules" \
        MODULE=${E3_MODULE_NAME} \
        LIBVERSION=${PV}
}


do_install() {
    make SYSROOT=${STAGING_DIR_HOST} \
        BUILD_ARCHS=${EPICS_TARGET_ARCH} \
        E3_REQUIRE_TOOLS="${STAGING_DIR_HOST}/epics-modules/require/share" \
        INSTALL_PREFIX="${D}" \
        MODULE=${E3_MODULE_NAME} \
        LIBVERSION=${PV} \
        install

    find ${D}${bindir} ${D}${libdir} ${D}${libdir} -maxdepth 1 -type f 2>/dev/null | while read f; do
        chrpath --replace /epics/lib/${EPICS_TARGET_ARCH}:${libdir} "$f" 2>/dev/null || true
    done
}
