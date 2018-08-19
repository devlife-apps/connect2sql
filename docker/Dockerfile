FROM openjdk:alpine
LABEL maintainer="DevLife Apps <support@devlife.app>"

ENV ANDROID_SDK_TOOLS_VERSION="3859397"
ENV ANDROID_HOME="/usr/local/android-sdk"
ENV ANDROID_VERSION=28
ENV ANDROID_BUILD_TOOLS_VERSION=28.0.2
ENV FASTLANE_VERSION=2.80.0
ENV GLIBC_VERSION=2.28-r0

ENV SDK_URL="https://dl.google.com/android/repository/sdk-tools-linux-${ANDROID_SDK_TOOLS_VERSION}.zip"
ENV DOWNLOAD_FILE=/tmp/sdk.zip

RUN echo "Installing standard dependencies..." \
    && apk add --no-cache --update --virtual=.build-dependencies \
        ca-certificates \
        bash \
        g++ \
        libstdc++ \
        make \
        ruby \
        ruby-dev \
        ruby-irb \
        unzip \
        wget \
    # ---------------------------------------------
    && echo "Installing glibc..." \
    && wget https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub -O /etc/apk/keys/sgerrand.rsa.pub \
    && wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-${GLIBC_VERSION}.apk -O /tmp/glibc.apk \
    && wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-bin-${GLIBC_VERSION}.apk -O /tmp/glibc-bin.apk \
    && apk add --no-cache /tmp/glibc.apk /tmp/glibc-bin.apk \
    # ---------------------------------------------
    && echo "Deleting alpine apk cache..." \
    && rm -rf /var/cache/apk/* \
    # ---------------------------------------------
    && echo "Installing fastlane..." \
    && gem install fastlane -N -v $FASTLANE_VERSION \
    # ---------------------------------------------
    && echo "Installing Android SDK..." \
    && mkdir -p "$ANDROID_HOME" \
    && wget -q -O "$DOWNLOAD_FILE" $SDK_URL \
    && unzip "$DOWNLOAD_FILE" -d "$ANDROID_HOME" \
    && rm "$DOWNLOAD_FILE" \
    && yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses \
    # ---------------------------------------------
    && echo "Installing Android build tools and platforms..." \
    && $ANDROID_HOME/tools/bin/sdkmanager "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
        "platforms;android-${ANDROID_VERSION}" \
        "platform-tools"