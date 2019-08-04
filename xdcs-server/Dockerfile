FROM jboss/wildfly:17.0.0.Final

EXPOSE 8080
EXPOSE 8081
EXPOSE 8082
EXPOSE 9990
EXPOSE 8787

RUN /opt/jboss/wildfly/bin/add-user.sh deploy deploy

ENV JAVA_OPTS="-server \
    -Xms64m -Xmx512m \
    -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m \
    -Djava.net.preferIPv4Stack=true \
    -agentlib:jdwp=transport=dt_socket,address=*:8787,server=y,suspend=n"

COPY debug-configure.cli /tmp/debug-configure.cli
RUN cd /opt/jboss/wildfly/bin && /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/debug-configure.cli

RUN rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history/current

COPY dev/xdcs-config.xml /opt/jboss/wildfly/
COPY dev/key.pub /opt/jboss/wildfly/
COPY dev/shadow /opt/jboss/wildfly/
COPY ./target/xdcs-server.war /opt/jboss/wildfly/standalone/deployments/

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
