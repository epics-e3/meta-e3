SUMMARY = "The PVXS EPICS module."
DESCRIPTION = "Library and CLI utilities acting as PVAccess protocol client and/or server."
HOMEPAGE = "https://github.com/epics-base/pvxs/"
LICENSE = "BSD-4-Clause-UC"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3775480a712fc46a69647678acb234cb"

DEPENDS += "libevent"

SRC_URI = "git://github.com/epics-base/pvxs.git;branch=master;protocol=https;"
SRCREV = "25ca43df4db909c0b1a445fb705fc81582617625"
PR = "r0"

inherit epics-module

# EPICS installs both libpvxs.so.X.Y (versioned) and libpvxs.so (unversioned full
# copy, not a symlink) — both are needed at runtime by EPICS module builds.
FILES:${PN} += "${libdir}/*.so ${sysconfdir}/epics /pvxs"
FILES:${PN}-dev:remove = "${libdir}/lib*.so"

SYSROOT_DIRS += "/pvxs"

do_configure:append() {
    # EPICS cross-compilation make rules look for compiled host tools at
    # $(EPICS_BASE)/bin/$(EPICS_HOST_ARCH)/; symlink that path to the native sysroot bin.
    mkdir -p ${S}/bin
    ln -sf ${STAGING_BINDIR_NATIVE} ${S}/bin/${EPICS_HOST_ARCH}
}

do_compile() {
    make ${PARALLEL_MAKE} \
        FINAL_LOCATION=/pvxs \
        IOCS_APPL_TOP=/pvxs \
        INSTALL_LOCATION=${S} install
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/bin/${EPICS_TARGET_ARCH}/* ${D}${bindir}/

    install -d ${D}${libdir}
    install -m 0644 ${S}/lib/${EPICS_TARGET_ARCH}/*.so* ${D}${libdir}/
    epics_set_rpath ${D}${bindir} /epics/lib/${EPICS_TARGET_ARCH}
    epics_set_rpath ${D}${libdir} /epics/lib/${EPICS_TARGET_ARCH}

    install -d ${D}${includedir}/pvxs
    install -m 0644 ${S}/include/pvxs/* ${D}${includedir}/pvxs/

    install -d ${D}/pvxs/dbd
    install -m 0644 ${S}/dbd/* ${D}/pvxs/dbd/

    install -d ${D}/pvxs/db
    install -m 0644 ${S}/db/* ${D}/pvxs/db/

    install -d ${D}${sysconfdir}/epics/env.d
    printf 'PVXS=/pvxs\n' \
        > ${D}${sysconfdir}/epics/env.d/10-pvxs.env
}
