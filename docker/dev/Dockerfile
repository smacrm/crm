FROM centos:7
MAINTAINER Hung Pham <phung@gnext.co.jp>

# RUN yum reinstall glibc-common -y
# RUN locale-gen en_US.UTF-8  
ENV LANG en_US.UTF-8  
ENV LANGUAGE en_US:en  
ENV LC_ALL en_US.UTF-8  
RUN echo "LANG=en_US.UTF-8" >> /etc/environment
RUN echo "LANGUAGE=en_US.UTF-8" >> /etc/environment
RUN echo "LC_ALL=en_US.UTF-8" >> /etc/environment
RUN echo "LC_CTYPE=en_US.UTF-8" >> /etc/environment

# Upgrading system
RUN yum -y update && yum -y install wget unzip pwgen expect libXext libXrender libXtst sudo

# Install Java.
ENV JAVA_VERSION 8u31
ENV BUILD_VERSION b13

# Downloading Java
RUN wget -qO- --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/$JAVA_VERSION-$BUILD_VERSION/jdk-$JAVA_VERSION-linux-x64.rpm" -O /tmp/jdk-8-linux-x64.rpm

RUN yum -y install /tmp/jdk-8-linux-x64.rpm && rm -f /tmp/jdk-8-linux-x64.rpm

RUN alternatives --install /usr/bin/java jar /usr/java/latest/bin/java 200000
RUN alternatives --install /usr/bin/javaws javaws /usr/java/latest/bin/javaws 200000
RUN alternatives --install /usr/bin/javac javac /usr/java/latest/bin/javac 200000

ENV JAVA_HOME /usr/java/latest

# Install glassfish 4.1.1
RUN wget -qO- http://download.java.net/glassfish/4.1.1/release/glassfish-4.1.1.zip -O /tmp/glassfish-4.1.1.zip &> /dev/null && \
    unzip -qq /tmp/glassfish-4.1.1.zip -d /opt && \
    rm -f /tmp/glassfish-4.1.1.zip

ENV PATH /opt/glassfish4/bin:/opt/app/bin:$PATH

RUN mkdir -p /opt/app/bin
RUN mkdir -p /opt/app/deploy

ADD bin/change_admin_password.sh /opt/app/bin/change_admin_password.sh
ADD bin/change_admin_password_func.sh /opt/app/bin/change_admin_password_func.sh
ADD bin/enable_secure_admin.sh /opt/app/bin/enable_secure_admin.sh
ADD bin/initialize-glassfish.sh /opt/app/bin/initialize-glassfish.sh
RUN chmod +x /opt/app/bin/*.sh

RUN /opt/app/bin/initialize-glassfish.sh

RUN echo 'root:root' | chpasswd

RUN mkdir -p /etc/service/glassfish
ADD bin/start-glassfish.sh /etc/service/glassfish/run
RUN chmod +x /etc/service/glassfish/run

# Configure glassfish
ADD lib/mysql-connector-java-5.1.34.jar /opt/glassfish4/glassfish/domains/domain1/lib/ext/mysql-connector-java-5.1.34.jar
ADD config/domain.xml /opt/glassfish4/glassfish/domains/domain1/config/domain.xml

# Install SASS
RUN yum -y install ruby
RUN gem install sass

# Install Netbeans 8.1
ADD config/state.xml /tmp/state.xml

RUN wget http://download.netbeans.org/netbeans/8.1/final/bundles/netbeans-8.1-javaee-linux.sh -O /tmp/netbeans.sh -q && \
    chmod +x /tmp/netbeans.sh
RUN echo 'Installing netbeans'
RUN /tmp/netbeans.sh --silent  && rm -rf /tmp/*
    
ADD config/netbean-run /usr/local/bin/netbeans
ADD config/netbeans.conf /usr/local/netbeans-8.1/etc/netbeans.conf

RUN chmod +x /usr/local/bin/netbeans
RUN mkdir -p /home/workspace

RUN  echo "developer:x:1000:1000:Developer,,,:/home/developer:/bin/bash" >> /etc/passwd && \
     echo "developer:x:1000:" >> /etc/group && \
     chmod u+w /etc/sudoers && \
     echo "includedir /etc/sudoers.d" >> /etc/sudoers && \
     chmod u-w /etc/sudoers && \
     echo "developer ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers.d/developer && \
     chmod 0440 /etc/sudoers.d/developer && \
     chown developer:developer -R /home/workspace && \
     chown developer:developer -R /opt/glassfish4

RUN rm -rf /usr/local/glassfish-4.1.1
RUN ln -s /opt/glassfish4/ /usr/local/glassfish-4.1.1

ENV HOME /home/workspace
WORKDIR /home/workspace

# 4848 (administration), 8080 (HTTP listener), 8181 (HTTPS listener), 9009 (JPDA debug port)
EXPOSE 4848 8080 8181 9009

CMD ["/etc/service/glassfish/run"]
#CMD "/usr/local/bin/netbeans"