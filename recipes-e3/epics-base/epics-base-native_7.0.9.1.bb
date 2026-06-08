DESCRIPTION = "EPICS Base Library"
LICENSE = "EPICS_LICENSE"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2eeea17a15fc6ba8501fdcec09b854dc"

SRC_URI = "git://github.com/epics-base/epics-base.git;branch=7.0;protocol=https;\
	         file://RULES_MODULES_echo_quotes.patch \
           file://RULES_BUILD_remove_mkdir.patch \
           file://initialise_pthreadInfo_attr.patch \
           file://test_dbtemplate_fix_relative_path_tests.patch \
           file://increase_default_errlog_size.patch \
           file://native-configure.sh"

#R7.0.9
SRCREV = "86154953f57b1796e7cb81bbc807eae120b9e840"
PR = "r0"

S = "${WORKDIR}/git"

inherit native

do_configure:append() {
    export libdir="${libdir}"
    export includedir="${includedir}" 
    export S="${S}"
    cp ${WORKDIR}/native-configure.sh ${S}/.
    bash native-configure.sh
}

do_compile() {
    make ${PARALLEL_MAKE} host
}

do_install() {
    echo "running do_install"
    EPICS_HOST_ARCH=$(perl src/tools/EpicsHostArch.pl)

    echo "installing directories"
    install -d ${D}${bindir}
    install -d ${D}${libdir}
    install -d ${D}${includedir}
    install -d ${D}${includedir}/epics

    echo "Installing binaries to ${D}${bindir}"
    install -m 0755 ${S}/bin/$EPICS_HOST_ARCH/* ${D}${bindir}/
    echo "Installing binaries to ${D}${libdir}"
    cp -a ${S}/lib/$EPICS_HOST_ARCH/*.so* ${D}${libdir}/
    cp -ar ${S}/lib/perl ${D}${libdir}/
    cp -a ${S}/include/* ${D}${includedir}/epics/
}
