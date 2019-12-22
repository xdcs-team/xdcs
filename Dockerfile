FROM maven:3-jdk-8 AS server-builder

WORKDIR /var/xdcs
COPY api /var/xdcs/api
COPY pom.xml /var/xdcs/
COPY xdcs-agent-api /var/xdcs/xdcs-agent-api
COPY xdcs-rest-api /var/xdcs/xdcs-rest-api
COPY xdcs-server /var/xdcs/xdcs-server
COPY xdcs-integration-tests/pom.xml /var/xdcs/xdcs-integration-tests/pom.xml
RUN mvn -T 2C -DskipTests=true clean install

################################################################################

FROM node:12 AS frontend-builder

WORKDIR /var/xdcs/xdcs-frontend

# Install dependencies
RUN npm install -g @angular/cli
RUN apt-get update && apt-get install -q -y zip

# Install dependencies
COPY xdcs-frontend/package.json /var/xdcs/xdcs-frontend/
COPY xdcs-frontend/package-lock.json /var/xdcs/xdcs-frontend/
RUN npm install

# Build frontend
COPY api /var/xdcs/api
COPY xdcs-frontend /var/xdcs/xdcs-frontend/

RUN npm run build-prod

################################################################################

FROM jboss/wildfly:18.0.0.Final

ARG XDCS_DB_JDBC_URL

COPY xdcs-server/setup /opt/jboss/wildfly/setup
RUN /opt/jboss/wildfly/setup/setup.sh

USER root
RUN rm -rf /opt/jboss/wildfly/setup
USER jboss

COPY --from=server-builder /var/xdcs/xdcs-server/target/xdcs-server.war \
    /opt/jboss/wildfly/standalone/deployments/

COPY --from=frontend-builder /var/xdcs/xdcs-frontend/dist/xdcs-frontend.war \
    /opt/jboss/wildfly/standalone/deployments/

# HTTP REST services
EXPOSE 8080

# GRPC services
EXPOSE 8081

# SSH services
EXPOSE 8082
