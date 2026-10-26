#!/usr/bin/env bash
# Preparação reproduzível da máquina cloud; não altera fontes nem lockfiles.
set -euo pipefail
cd /workspace/mundinho-do-vivi_v4
source scripts/cloud-env.sh
mkdir -p /workspace/.tools/downloads "$ANDROID_HOME" "$ANDROID_USER_HOME" "$GRADLE_USER_HOME" "$XDG_DATA_HOME" "$XDG_CACHE_HOME" "$XDG_BIN_HOME"

if [[ ! -x "$JAVA_HOME/bin/javac" ]]; then
  curl -fsSL --retry 2 --max-time 600 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.17%2B10/OpenJDK17U-jdk_x64_linux_hotspot_17.0.17_10.tar.gz' -o /workspace/.tools/downloads/jdk17.tar.gz
  printf '%s  %s\n' '992f96e7995075ac7636bb1a8de52b0c61d71ed3137fafc979ab96b4ab78dd75' /workspace/.tools/downloads/jdk17.tar.gz | sha256sum -c -
  tar -xzf /workspace/.tools/downloads/jdk17.tar.gz -C /workspace/.tools
fi
if [[ ! -x "$ANDROID_HOME/cmdline-tools/latest/bin/android" ]]; then
  curl -fsSL --retry 2 --max-time 600 https://dl.google.com/android/repository/commandlinetools-linux-16111833_latest.zip -o /workspace/.tools/downloads/android-tools-current.zip
  # SHA256 calculado após verificar o SHA1 publicado pelo repositório oficial do Android.
  printf '%s  %s\n' '0877a1d048fe4a24efe2eff536ca4223f7adeb58648bb81909d33c446918cfa8' /workspace/.tools/downloads/android-tools-current.zip | sha256sum -c -
  sdk_extract=$(mktemp -d /workspace/.tools/android.XXXXXX)
  unzip -q /workspace/.tools/downloads/android-tools-current.zip -d "$sdk_extract"
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  mv "$sdk_extract/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
  rmdir "$sdk_extract"
fi

# JVM usa explicitamente o proxy injetado e o truststore público do sistema,
# mantendo validação TLS. Não copia credenciais nem valores de segredos.
python3 - <<'PY'
import os, pathlib, urllib.parse
p=pathlib.Path(os.environ['GRADLE_USER_HOME'])/'gradle.properties'
props=p.read_text().splitlines() if p.exists() else []
updates={}
u=urllib.parse.urlsplit(os.environ.get('HTTPS_PROXY',os.environ.get('HTTP_PROXY','')))
if u.hostname:
    for protocol in ('http','https'):
        updates[f'systemProp.{protocol}.proxyHost']=u.hostname
        updates[f'systemProp.{protocol}.proxyPort']=str(u.port or 80)
    updates['systemProp.http.nonProxyHosts']='localhost|127.*'
    system_store=pathlib.Path('/etc/ssl/certs/java/cacerts')
    if system_store.is_file(): updates['systemProp.javax.net.ssl.trustStore']=str(system_store)
props=[line for line in props if line.split('=',1)[0] not in updates]
props += [f'{key}={value}' for key,value in updates.items()]
p.write_text('\n'.join(props)+'\n')
PY

android --no-metrics --sdk="$ANDROID_HOME" sdk install 'platforms;android-35'
android --no-metrics --sdk="$ANDROID_HOME" sdk install 'build-tools;35.0.0'
android --no-metrics --sdk="$ANDROID_HOME" sdk install 'platform-tools'
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --no-daemon --max-workers=4
