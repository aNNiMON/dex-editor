
PRO_NAME := Term
OUT_DIR := bin


#java compile 
JC := javac
SRC := src
JFLAGS = -g -d $(OUT_DIR) -encoding utf-8 -target 1.5 -classpath bin -sourcepath src
SOURCE_FILES := $(shell find $(SRC) -name "*.java")
#dex
DEX := dx
OUT_CLASSES_DEX := $(OUT_DIR)/Test.jar
DEXFLAGS := --dex --no-optimize --output=$(OUT_CLASSES_DEX)






$(OUT_DIR)/%.class: $(SRC)/%.java
	$(JC) $(JFLAGS) $<

.PHONY: all clean jar

all: $(OUT_CLASSES_DEX)


$(OUT_CLASSES_DEX): $(OUT_DIR) $(subst $(SRC)/,$(OUT_DIR)/,$(SOURCE_FILES:.java=.class))
	@echo -e "\033[33m CLASSES TO DEX...\033[0m "

$(OUT_DIR): 
	@if [ ! -z "$(OUT_DIR)" -a ! -d "$(OUT_DIR)" ];then\
		echo "mkdir $(OUT_DIR)";\
		mkdir $(OUT_DIR);\
		fi


test:
	cd bin;\
		java  Test

jar:
	jar cvf dexlib.jar -C bin .







clean:
	$(RM) -r $(OUT_DIR)
