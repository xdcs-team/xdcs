FROM jboss/wildfly:17.0.0.Final

COPY ./target/xdcs-server.war /opt/jboss/wildfly/standalone/deployments/

EXPOSE 8080
EXPOSE 8081
EXPOSE 9990
EXPOSE 8787

RUN /opt/jboss/wildfly/bin/add-user.sh deploy deploy

ENV JAVA_OPTS="-server \
    -Xms64m -Xmx512m \
    -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m \
    -Djava.net.preferIPv4Stack=true \
    -agentlib:jdwp=transport=dt_socket,address=*:8787,server=y,suspend=n"

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
