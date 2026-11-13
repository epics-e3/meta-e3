SUMMARY = "The require3 e3 module"
DESCRIPTION = "EPICS module for dynamically loading functionality at runtime"
HOMEPAGE = "https://github.com/epics-e3/require3"
LICENSE = "LGPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3000208d539ec061b899bce1d9ce9404"

DEPENDS += "pvxs"

SRC_URI = "git://github.com/epics-e3/require3.git;branch=main;protocol=https;"
SRCREV = "ab0f44bdb94477e56e5b06f7f2cb8889739820e7"
PR = "r0"

inherit epics-module

E3_REQUIRE_LOCATION = "/epics-modules/${PN}"

FILES:${PN} += "/epics-modules ${sysconfdir}/epics"
SYSROOT_DIRS += "/epics-modules/require3"
FILES:${PN} += "${libdir}/*.so"
FILES:${PN}-dev:remove = "${libdir}/lib*.so"
INSANE_SKIP:${PN} += "dev-so rpaths libdir"

do_compile() {
    make ${PARALLEL_MAKE} \
        SYSROOT=${STAGING_DIR_HOST} \
        CC="${CC} ${DEBUG_PREFIX_MAP}" \
        CCC="${CXX} ${DEBUG_PREFIX_MAP}" \
        AR="${AR} -rc" \
        RANLIB="${RANLIB}" \
        LD="${LD}" \
        LDFLAGS="${LDFLAGS}" \
        INSTALL_LOCATION=${S}/epics-modules/${PN} \
        INSTALL_BIN=${S}/bin \
        INSTALL_SHRLIB=${S}/lib \
        INSTALL_INCLUDE=${S}/include
}

do_install() {
    install -d ${D}${E3_REQUIRE_LOCATION}
    cp -r --no-preserve=ownership --preserve=mode,timestamps \
        ${S}/epics-modules/${PN}/. ${D}${E3_REQUIRE_LOCATION}/

    if [ -d ${S}/bin ] && [ "$(ls -A ${S}/bin 2>/dev/null)" ]; then
        install -d ${D}${bindir}
        install -m 0755 ${S}/bin/* ${D}${bindir}/
    fi

    if ls ${S}/lib/*.so* 2>/dev/null | grep -q .; then
        install -d ${D}${libdir}
        install -m 0755 ${S}/lib/*.so* ${D}${libdir}/
    fi
    epics_set_rpath ${D}${bindir} /epics/lib/${EPICS_TARGET_ARCH}
    epics_set_rpath ${D}${libdir} /epics/lib/${EPICS_TARGET_ARCH}

    # RELEASE.local points at the sysroot of this build; point it at the
    # target locations instead (buildpaths QA).
    sed -i 's#${STAGING_DIR_HOST}##' \
        ${D}${E3_REQUIRE_LOCATION}/configure/RELEASE.local

    if [ -d ${S}/include ] && [ "$(ls -A ${S}/include 2>/dev/null)" ]; then
        install -d ${D}${includedir}
        cp -r --no-preserve=ownership --preserve=mode,timestamps \
            ${S}/include/. ${D}${includedir}/
    fi

    install -d ${D}${sysconfdir}/epics/env.d
    cat > ${D}${sysconfdir}/epics/env.d/20-require.env <<EOF
E3_REQUIRE_VERSION=${PV}
E3_REQUIRE_BIN=${bindir}
E3_REQUIRE_LIB=${libdir}
E3_REQUIRE_DB=/epics-modules/require3/db
E3_REQUIRE_DBD=/epics-modules/require3/dbd
REQUIRE_MODULE_PATH=/epics-modules/
EPICS_HOST_ARCH=${EPICS_TARGET_ARCH}
EOF
}
