DESCRIPTION = "EPICS Base Library"
LICENSE = "EPICS_LICENSE"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2eeea17a15fc6ba8501fdcec09b854dc"

SRC_URI = "git://github.com/epics-base/epics-base.git;branch=7.0;protocol=https;\
	         file://RULES_MODULES_echo_quotes.patch \
           file://RULES_BUILD_remove_mkdir.patch \
           file://initialise_pthreadInfo_attr.patch \
           file://test_dbtemplate_fix_relative_path_tests.patch \
           file://increase_default_errlog_size.patch \
           file://configure.sh \
           file://epics.sh"

SRCREV = "86154953f57b1796e7cb81bbc807eae120b9e840"
PR = "r0"

S = "${WORKDIR}/git"

inherit perlnative

DEPENDS += "epics-base-native"

# The EPICS tree contains a mix of ELF binaries, Perl scripts, makefiles and
# data files. Disable debug splitting and stripping to prevent objcopy from
# fatally failing on non-ELF files during do_package.
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"

# The full EPICS tree is installed under /epics/ (non-standard path).
# Assign everything there to the main package and suppress auto-splitting
# into -dev/-staticdev, which would otherwise match /epics/include/ and
# /epics/lib/*.a via default patterns.
FILES:${PN} = "/epics ${sysconfdir}/profile.d ${sysconfdir}/epics"
SYSROOT_DIRS += "/epics"
FILES:${PN}-dev = ""
FILES:${PN}-staticdev = ""

# Skip QA checks that don't apply to a non-standard EPICS tree layout.
INSANE_SKIP:${PN} += "dev-so libdir rpaths staticdev file-rdeps"

export PERL5LIB = "${STAGING_LIBDIR_NATIVE}/perl"

do_configure:append() {
    export TARGET_ARCH="${TARGET_ARCH}"
    export TARGET_PREFIX="${TARGET_PREFIX}"
    export STAGING_BINDIR_NATIVE="${STAGING_BINDIR_NATIVE}"
    export libdir="${libdir}"
    export includedir="${includedir}"
    export S="${S}"
    cp ${WORKDIR}/configure.sh ${S}/.
    bash configure.sh

    # EPICS cross-compilation make rules look for compiled host tools at
    # $(EPICS_BASE)/bin/$(EPICS_HOST_ARCH)/, not at EPICS_HOST_BIN.
    # Symlink that path to the native sysroot bin so tools like e_flex are found.
    mkdir -p ${S}/epics/bin
    ln -sf ${STAGING_BINDIR_NATIVE} ${S}/epics/bin/linux-x86_64

    # Populate epics/include with all native headers before the cross-build runs.
    # BUILD_ARCHS=linux-ppc64 skips the host build phase that normally installs
    # module headers (e.g. dbChannel.h). Copying (not symlinking) avoids the
    # absolute-path symlink that sstate rejects in the sysroot.
    mkdir -p ${S}/epics/include
    cp -r ${STAGING_DIR_NATIVE}/usr/include/epics/. ${S}/epics/include/
}

do_compile() {
    make ${PARALLEL_MAKE} SYSROOT=${STAGING_DIR_HOST} \
        CC="${CC}" \
        CCC="${CXX}" \
        AR="${AR} -rc" \
        RANLIB="${RANLIB}" \
        LD="${LD}" \
        LDFLAGS="${LDFLAGS}"
}

do_install() {
    # Remove binary tools symlink
    rm -Rf ${S}/epics/bin/linux-x86_64

    install -d ${D}/epics/
    cp -r --no-preserve=ownership --preserve=mode,links,timestamps ${S}/epics/* ${D}/epics/.
    echo "${PV}" > ${D}/epics/EPICS_BASE_VERSION

    install -d ${D}${sysconfdir}/profile.d
    install -m 0644 ${WORKDIR}/epics.sh ${D}${sysconfdir}/profile.d/epics.sh

    install -d ${D}${sysconfdir}/epics/env.d
    printf 'EPICS_BASE=/epics\nEPICS_BASE_VERSION=%s\n' "${PV}" \
        > ${D}${sysconfdir}/epics/env.d/00-epics-base.env
}
