# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY pom.xml .
COPY bestellung-ejb bestellung-ejb
COPY bestellung-web bestellung-web
COPY bestellung-ear bestellung-ear
COPY bestellung-bpel bestellung-bpel
RUN mvn -q -pl bestellung-ear -am clean package -DskipTests

COPY docker/wildfly/oracle-libs docker/wildfly/oracle-libs
RUN mvn -q -f docker/wildfly/oracle-libs/pom.xml dependency:copy-dependencies \
        -DoutputDirectory=/workspace/resources/oracle -DincludeScope=runtime -Dmdep.stripVersion=true

FROM quay.io/wildfly/wildfly:latest-jdk17

COPY --chown=jboss:jboss docker/wildfly/oracle-module/module.xml ${JBOSS_HOME}/modules/com/oracle/main/module.xml
COPY --chown=jboss:jboss --from=builder /workspace/resources/oracle/ojdbc11.jar ${JBOSS_HOME}/modules/com/oracle/main/ojdbc11.jar

COPY --chown=jboss:jboss docker/wildfly/configure.cli /tmp/configure.cli
RUN ${JBOSS_HOME}/bin/jboss-cli.sh --file=/tmp/configure.cli \
 && rm /tmp/configure.cli

COPY --chown=jboss:jboss --from=builder /workspace/bestellung-ear/target/bestellung.ear ${JBOSS_HOME}/standalone/deployments/bestellung.ear

# ode.base.url/app.base.url passen bereits per Default (siehe BestellungOdeClient/ApiClient) zum
# Docker-Compose-Netzwerk (Service "ode", eigener Loopback fuer die REST-Fassade) - hier trotzdem
# explizit gesetzt, damit die Konfiguration sichtbar/aenderbar bleibt.
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-c", "standalone-full.xml", "-Dode.base.url=http://ode:8080/ode", "-Dapp.base.url=http://localhost:8080/bestellung/api"]
