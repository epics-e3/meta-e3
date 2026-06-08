#!/bin/bash

EPICS_HOST_ARCH=$(perl src/tools/EpicsHostArch.pl)

cat <<EOF >>configure/os/CONFIG_SITE.Common.linuxCommon
POSIX_CFLAGS += -std=gnu17

# --disable-new-dtags is required to avoid LD_LIBRARY_PATH overrride RPATH settings
OP_SYS_LDFLAGS += -Wl,--disable-new-dtags -Wl,-rpath,${libdir} -Wl,-rpath-link,${libdir} -Wl,-rpath-link,${EPICS_BASE}/lib/${EPICS_HOST_ARCH}
OP_SYS_INCLUDES += -I${includedir}
EOF
