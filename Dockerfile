FROM debian:latest
MAINTAINER Anton Naumov <anton.naumow@gmail.com>

ENV DEBIAN_FRONTEND noninteractive

RUN echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list \
	&& echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list \
	&& apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886 \
    && apt-get -y update \
    && echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections \
    && echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections \
    && apt-get -y install software-properties-common oracle-java7-installer oracle-java7-set-default

WORKDIR /srv

ADD target/scala-2.11/FinCached-assembly-1.0.0-SNAPSHOT.jar ./

ENV JAVA_OPTS="-Xms384m -Xmx768m -XX:MaxPermSize=384m -Xss512k -XX:+UseCompressedOops"

CMD java $JAVA_OPTS -jar FinCached-assembly-1.0.0-SNAPSHOT.jar
