
PRO_NAME := Dalvik-Bytecode-Editor
OUT_DIR := bin


#java compile 
JC := javac
JFLAGS = -g -d $(OUT_DIR) -bootclasspath $(ANDROID_API) -classpath libs/dexlib.jar -sourcepath $(GEN):$(SRC)
SOURCE_FILES = $(shell find $(SRC) -name "*.java")
GEN := gen
SRC := src
ANDROID_API := /data/java/classlib/android2.2.jar
#dex
DEX := dx
OUT_CLASSES_DEX := $(OUT_DIR)/classes.dex
DEXFLAGS := --dex --no-optimize --verbose --output=$(OUT_CLASSES_DEX) libs/dexlib.jar

#aapt
MANIFEST :=AndroidManifest.xml
RESOURCES_FILE := $(OUT_DIR)/resources.ap_
RESOURCES_UNCOMPILE := $(shell find res )
RES := res

#apkbuilder

UNSIGNED_APK := $(OUT_DIR)/$(PRO_NAME).unsigned.apk

#apksigner

SIGNED_APK := $(OUT_DIR)/$(PRO_NAME).apk


$(OUT_DIR)/%.class: $(SRC)/%.java 
	$(JC) $(JFLAGS) $<

.PHONY: all install run test clean


all: $(SIGNED_APK)


$(OUT_CLASSES_DEX):$(RESOURCES_FILE) $(subst $(SRC)/,$(OUT_DIR)/,$(SOURCE_FILES:.java=.class))
	@echo -e "\033[33m CLASSES TO DEX...\033[0m "
	$(DEX) $(DEXFLAGS) $(OUT_DIR)

$(RESOURCES_FILE) : $(RESOURCES_UNCOMPILE) $(MANIFEST)
	@if [ ! -z "$(GEN)" -a ! -d "$(GEN)" ];then\
		echo "mkdir $(GEN)";\
		mkdir $(GEN);\
		fi
	@if [ ! -z "$(OUT_DIR)" -a ! -d "$(OUT_DIR)" ];then\
		echo "mkdir $(OUT_DIR)";\
		mkdir $(OUT_DIR);\
		fi
	@echo -e "\033[33m COMPILE RESOURCES...\033[0m "
	aapt p -m -J $(GEN) -M $(MANIFEST)  -S $(RES) -I $(ANDROID_API) -f -F $(RESOURCES_FILE)



$(SIGNED_APK): $(OUT_CLASSES_DEX) $(RESOURCES_FILE)
	@echo -e "\033[33m BUILDING APK...\033[0m "
	apkbuilder $(UNSIGNED_APK) -u -z $(RESOURCES_FILE) -f $(OUT_CLASSES_DEX) -rf $(SRC)
	@echo -e "\033[33m SIGNING APK...\033[0m "
	apksigner $(UNSIGNED_APK) $(SIGNED_APK)
	$(RM) $(UNSIGNED_APK)


PACKAGE := $(shell grep "\ *package\ *=\ *\"" $(MANIFEST)|sed  "s/\ * package\ *=\ *//g;s/\ *\ .*//g;s/\"\ *>*//g")


ACTIVITY := $(shell grep  "\ *<activity\ *android\:name" $(MANIFEST)|sed "s/\ *<activity\ *android\:name\ *=\ *//g;s/\ *\ .*//g;s/\"\.*//g")



install:
	@echo -e "\033[33m INSTALLING APK...\033[0m "
	@echo `pm install -r $(SIGNED_APK)`


run:
	@echo -e "\033[33m RUNNING...\033[0m "
	@echo ` am start -n $(PACKAGE)/$(PACKAGE).$(word 1,$(ACTIVITY))`


test:
	@echo -e "\033[33m INSTALLING APK...\033[0m "
	@echo `pm install -r $(SIGNED_APK)`
	@echo -e "\033[33m RUNNING...\033[0m "
	@echo `am start -n $(PACKAGE)/$(PACKAGE).$(word 1,$(ACTIVITY))`



clean:
	$(RM) $(OUT_CLASSES_DEX)
	$(RM) $(RESOURCES_FILE)
	$(RM) $(SIGNED_APK)
	$(RM) -r $(OUT_DIR)
	$(RM) -r $(GEN)
