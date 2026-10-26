#!/usr/bin/env bash
# Usar com: source scripts/cloud-env.sh
export JAVA_HOME='/workspace/.tools/jdk-17.0.17+10'
export ANDROID_HOME='/workspace/.android-sdk'
export ANDROID_USER_HOME='/workspace/.android'
export ANDROID_PREFS_ROOT='/workspace'
export GRADLE_USER_HOME='/workspace/.gradle'
export XDG_DATA_HOME='/workspace/.local/share'
export XDG_CACHE_HOME='/workspace/.cache'
export XDG_BIN_HOME='/workspace/.local/bin'
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
