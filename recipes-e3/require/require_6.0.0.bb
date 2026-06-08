DESCRIPTION = "EPICS module for dynamically loading functionality at runtime"
HOMEPAGE = "https://gitlab.esss.lu.se/epics-modules/require"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=62ae04c770d5b4f4180e42b47063ab03"

SRC_URI = "git://gitlab.esss.lu.se/epics-modules/require.git;branch=main;protocol=https;"

SRCREV = "83f71357ea7e5899445101deaa85f0fbd9f2df09"
PR = "r0"
S = "${WORKDIR}/git"

inherit epics-module

DEPENDS += "pvxs"

E3_REQUIRE_LOCATION = "/epics-modules/${PN}"

FILES:${PN} += "/epics-modules ${sysconfdir}/epics"
SYSROOT_DIRS += "/epics-modules/require"
FILES:${PN} += "${libdir}/*.so"
FILES:${PN}-dev:remove = "${libdir}/lib*.so"
INSANE_SKIP:${PN} += "dev-so rpaths libdir"

do_configure:append() {
    echo "PVXS:=${STAGING_DIR_HOST}/pvxs" >> configure/RELEASE.local
}

do_compile() {
    make ${PARALLEL_MAKE} \
        SYSROOT=${STAGING_DIR_HOST} \
        CC="${CC}" \
        CCC="${CXX}" \
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

    if [ -d ${S}/include ] && [ "$(ls -A ${S}/include 2>/dev/null)" ]; then
        install -d ${D}${includedir}
        cp -r --no-preserve=ownership --preserve=mode,timestamps \
            ${S}/include/. ${D}${includedir}/
    fi

    install -d ${D}${sysconfdir}/epics/env.d
    cat > ${D}${sysconfdir}/epics/env.d/20-require.env <<EOF
E3_REQUIRE_VERSION=${PV}
E3_REQUIRE_BIN=/usr/bin
E3_REQUIRE_LIB=/usr/lib
E3_REQUIRE_DB=/epics-modules/require/db
E3_REQUIRE_DBD=/epics-modules/require/dbd
REQUIRE_MODULE_PATH=/epics-modules/
EPICS_HOST_ARCH=${EPICS_TARGET_ARCH}
EOF
}
