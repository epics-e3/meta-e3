include $(E3_REQUIRE_TOOLS)/driver.makefile

USR_CPPFLAGS += -DUSE_TYPED_RSET

DEVIOCSTATS:=devIocStats

HEADERS += $(DEVIOCSTATS)/os/default/devIocStatsOSD.h
HEADERS += $(DEVIOCSTATS)/devIocStats.h

SOURCES += $(DEVIOCSTATS)/devIocStatsAnalog.c
SOURCES += $(DEVIOCSTATS)/devIocStatsString.c
SOURCES += $(DEVIOCSTATS)/devIocStatsWaveform.c
SOURCES += $(DEVIOCSTATS)/devIocStatsSub.c
SOURCES += $(DEVIOCSTATS)/devIocStatsTest.c

# iocStats has many source codes, which has the same name in
# terms of archtecture. driver.makefile isn't clever enough to
# understand its relations, so we have to use the specific
# source codes order in the same as the original makefile
# as follows:

SOURCES += $(DEVIOCSTATS)/os/Linux/osdCpuUsage.c
SOURCES += $(DEVIOCSTATS)/os/Linux/osdCpuUtilization.c
SOURCES += $(DEVIOCSTATS)/os/Linux/osdFdUsage.c
SOURCES += $(DEVIOCSTATS)/os/Linux/osdMemUsage.c
SOURCES += $(DEVIOCSTATS)/os/default/osdWorkspaceUsage.c
SOURCES += $(DEVIOCSTATS)/os/default/osdClustInfo.c
SOURCES += $(DEVIOCSTATS)/os/default/osdSuspTasks.c
SOURCES += $(DEVIOCSTATS)/os/default/osdIFErrors.c
SOURCES += $(DEVIOCSTATS)/os/default/osdBootInfo.c
SOURCES += $(DEVIOCSTATS)/os/posix/osdSystemInfo.c
SOURCES += $(DEVIOCSTATS)/os/posix/osdHostInfo.c
SOURCES += $(DEVIOCSTATS)/os/posix/osdPIDInfo.c

DBDS    += $(DEVIOCSTATS)/devIocStats.dbd

HEADERS += $(DEVIOCSTATS)/devIocStats.h

IOCADMIN:= iocAdmin
IOCADMINDB:=$(IOCADMIN)/Db


SCRIPTS += iocsh/iocStats.iocsh

TEMPLATES += $(wildcard template/*.db)
TEMPLATES += $(wildcard template/*.template)

USR_DBFLAGS += -I . -I ..
USR_DBFLAGS += -I$(EPICS_BASE)/db
USR_DBFLAGS += -I$(where_am_I)/template
USR_DBFLAGS += -I$(IOCADMINDB)
USR_DBFLAGS += -I$(COMMON_DIR)

SUBS += $(wildcard template/*.substitutions)
SUBS += $(COMMON_DIR)/siteEnvVars.substitutions
SUBS += $(IOCADMINDB)/epicsPVAEnvVars.substitutions

$(COMMON_DIR)/iocAdminSoft-ess.db: $(COMMON_DIR)/siteEnvVars.db $(COMMON_DIR)/epicsPVAEnvVars.db

$(COMMON_DIR)/siteEnvVars.substitutions: $(EPICS_BASE)/configure/CONFIG_SITE_ENV $(EPICS_BASE)/configure/CONFIG_ENV
	@echo Expanding siteEnvVars.substitutions from $^....
	@$(RM) $@
	@$(PERL) $(where_am_I)/$(IOCADMINDB)/genSiteEnvVars.pl $^ > $@
