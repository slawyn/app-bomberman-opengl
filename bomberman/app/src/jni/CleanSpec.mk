# CleanSpec.mk
LOCAL_PATH:= $(call my-dir)

# Include the standard clean rules
include $(CLEAR_VARS)

# Define custom clean rules
# Use the '$(call all-subdir-makefiles)' to include all subdirectories
$(call all-subdir-makefiles)

# Custom clean rules
# Add custom clean rules here
# For example, to remove a specific directory:
$(shell rm -rf $(LOCAL_PATH)/build)

# To remove specific files:
$(shell rm -f $(LOCAL_PATH)/libs/armeabi-v7a/*.so)
$(shell rm -f $(LOCAL_PATH)/libs/x86/*.so)
