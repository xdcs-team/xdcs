FROM jboss/wildfly:18.0.0.Final

EXPOSE 8080
EXPOSE 8081
EXPOSE 8082
EXPOSE 9990
EXPOSE 8787

USER root
RUN yum install -y wget nano less
USER jboss

ARG as_root=/opt/jboss/wildfly
ARG configure_cli_path=${as_root}/configure.cli

COPY dev/setup ${as_root}/setup
RUN ${as_root}/setup/setup.sh

COPY dev/xdcs-config.xml ${as_root}/
COPY dev/key.pub ${as_root}/
COPY dev/shadow ${as_root}/
COPY ./target/xdcs-server.war ${as_root}/standalone/deployments/

ENV JAVA_OPTS="-server \
    -Xms512M -Xmx2G \
    -XX:MetaspaceSize=512M -XX:MaxMetaspaceSize=1G \
    -Djava.net.preferIPv4Stack=true \
    -agentlib:jdwp=transport=dt_socket,address=*:8787,server=y,suspend=n"

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
