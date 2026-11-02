DESCRIPTION = "EPICS IOC Status and Control"
HOMEPAGE = "https://github.com/epics-modules/iocStats"
LICENSE = "EPICS_LICENSE"
LIC_FILES_CHKSUM = "file://LICENSE;md5=76d18f9132055ed510b481f6f211e0d7"

SRC_URI = "git://github.com/epics-modules/iocStats.git;branch=master;protocol=https; \
           file://Makefile \
           file://iocStats.iocsh \
           file://iocAdminSoft-ess.substitutions \
           file://iocGeneralTime.template \
           file://iocQueue.db \
           file://ioc.template \
           file://make_gen_vars_local.patch"

SRCREV = "c7ae6119661264f351c0c3f37a4ab042fa32f25d"
PR = "r0"

inherit e3

E3_MODULE_NAME = "iocstats"

INSANE_SKIP:${PN} += "dev-so rpaths"


do_configure:append() {
    cp ${UNPACKDIR}/Makefile ${S}/Makefile

    mkdir -p ${S}/iocsh
    cp ${UNPACKDIR}/iocStats.iocsh ${S}/iocsh/

    mkdir -p ${S}/template
    cp ${UNPACKDIR}/iocAdminSoft-ess.substitutions ${S}/template/
    cp ${UNPACKDIR}/iocGeneralTime.template ${S}/template/
    cp ${UNPACKDIR}/iocQueue.db ${S}/template/
    cp ${UNPACKDIR}/ioc.template ${S}/template/
}

