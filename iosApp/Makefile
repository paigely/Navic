PACKAGE_FORMAT = ipa
FINALPACKAGE = 1
TARGET = iphone:latest:14.1
ARCHS = arm64

THEOS_PACKAGE_SCHEME = rootless
INSTALL_TARGET_PROCESSES = Navic

include $(THEOS)/makefiles/common.mk

XCODEPROJ_NAME = iosApp

Navic_XCODE_SCHEME = iosApp
Navic_CODESIGN_FLAGS = -SiosApp/entitlements.xml`

include $(THEOS_MAKE_PATH)/xcodeproj.mk
