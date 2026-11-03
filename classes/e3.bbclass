inherit epics-module
DEPENDS += "require"

# This is a conviniance, as every module will install these files.
FILES:${PN} += "/epics-modules/${E3_MODULE_NAME}"
# require loads module libraries by their unversioned name.
FILES:${PN} += "${libdir}/*.so"
FILES:${PN}-dev:remove = "${libdir}/lib*.so"
INSANE_SKIP:${PN} += "libdir dev-so"
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
        CMD_CFLAGS="${DEBUG_PREFIX_MAP}" \
        CMD_CPPFLAGS="${DEBUG_PREFIX_MAP}" \
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

    # driver.makefile installs to ${INSTALL_PREFIX}/lib and /include. Move
    # them to the standard directories (usrmerge QA; /lib is also not on the
    # library search path of 64-bit machines).
    if [ -d ${D}/lib ]; then
        install -d ${D}${libdir}
        mv ${D}/lib/* ${D}${libdir}/
        rmdir ${D}/lib
    fi
    if [ -d ${D}/include ]; then
        install -d ${D}${includedir}
        mv ${D}/include/* ${D}${includedir}/
        rmdir ${D}/include
    fi

    find ${D}${bindir} ${D}${libdir} ${D}${libdir} -maxdepth 1 -type f 2>/dev/null | while read f; do
        chrpath --replace /epics/lib/${EPICS_TARGET_ARCH}:${libdir} "$f" 2>/dev/null || true
    done
}
