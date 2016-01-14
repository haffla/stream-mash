FROM ubuntu:14.04
MAINTAINER Jakob Pupke <jakob.pupke@gmail.com>

RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823

RUN apt-get -y install apt-transport-https software-properties-common

#Java Webup8 repo
RUN add-apt-repository -y ppa:webupd8team/java
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections

RUN apt-get -yqq update

RUN apt-get -y install sbt git oracle-java8-installer
RUN apt-get -y install nodejs npm
RUN ln -s /usr/bin/nodejs /usr/bin/node

ADD . /opt/stream
WORKDIR /opt/stream

RUN npm install
RUN npm install -g gulp
RUN gulp webpack
RUN gulp uglify

# expose port
EXPOSE 9000

# start app
CMD [ "sbt", "run" ]