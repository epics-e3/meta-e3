SUMMARY = "EPICS Base Library"
DESCRIPTION = "This is the main core of EPICS."
HOMEPAGE = "https://epics-controls.org"
LICENSE = "EPICS_LICENSE"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2eeea17a15fc6ba8501fdcec09b854dc"

DEPENDS += "epics-base-native patchelf-native"

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

inherit perlnative

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
    cp ${UNPACKDIR}/configure.sh ${S}/.
    bash configure.sh

    # Remove old build as headers of a previous build are read-only
    # and cannot be overwritten.
    rm -rf ${S}/epics

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
        CMD_CFLAGS="${DEBUG_PREFIX_MAP}" \
        CMD_CPPFLAGS="${DEBUG_PREFIX_MAP}" \
        AR="${AR} -rc" \
        RANLIB="${RANLIB}" \
        LD="${LD}" \
        LDFLAGS="${LDFLAGS}" \
        FINAL_LOCATION=/epics \
        IOCS_APPL_TOP=/epics
}

do_install() {
    install -d ${D}/epics/
    cp -r --no-preserve=ownership --preserve=mode,links,timestamps ${S}/epics/* ${D}/epics/.
    # Remove the host tools symlinks, they aren't used after do_compile step.
    rm -f ${D}/epics/bin/linux-x86_64
    echo "${PV}" > ${D}/epics/EPICS_BASE_VERSION

    # Drop build-host paths from the installed tree (pkgconfig and buildpaths
    # QA checks):
    #  - pkg-config files record the toolchain and LDFLAGS used for the build.
    #  - CONFIG_SITE.local holds the paths set by configure.sh; modules set
    #    their own TOOLS and EPICS_HOST_BIN (see epics-module.bbclass).
    #  - TOOLCHAIN.* is preprocessor output, with line markers.
    sed -i -E -e 's# -f(debug|macro|file)-prefix-map=[^ ]*##g' \
        -e 's# --sysroot=[^ ]*##g' \
        ${D}/epics/lib/pkgconfig/*.pc
    sed -i -E '/^(INSTALL_LOCATION|EPICS_HOST_BIN|TOOLS)=/d' \
        ${D}/epics/configure/CONFIG_SITE.local
    sed -i '/^# [0-9]/d' ${D}/epics/cfg/TOOLCHAIN.*

    # Set the RUNPATH, which EPICS links with the build-tree location, to the
    # target location of the libraries (see epics_set_rpath in
    # epics-module.bbclass).
    for dir in ${D}/epics/bin/* ${D}/epics/lib/*; do
        arch=$(basename "$dir")
        find "$dir" -maxdepth 1 -type f | while read f; do
            if [ "$(head -c 4 "$f" | tail -c 3)" = "ELF" ]; then
                patchelf --set-rpath /epics/lib/$arch "$f"
            fi
        done
    done

    install -d ${D}${sysconfdir}/profile.d
    install -m 0644 ${UNPACKDIR}/epics.sh ${D}${sysconfdir}/profile.d/epics.sh

    install -d ${D}${sysconfdir}/epics/env.d
    printf 'EPICS_BASE=/epics\nEPICS_BASE_VERSION=%s\n' "${PV}" \
        > ${D}${sysconfdir}/epics/env.d/00-epics-base.env
}
