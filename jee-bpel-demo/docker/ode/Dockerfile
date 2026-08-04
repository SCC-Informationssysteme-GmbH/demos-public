# syntax=docker/dockerfile:1
#
# Apache ODE 1.3.8 (letzter Release, 2018) auf Tomcat 8.5/JDK8 - bewusst
# isolierter Legacy-Stack, getrennt vom Jakarta-EE-10/JDK17-Container (WildFly).
FROM tomcat:8.5-jdk8-temurin

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/*

# ODE als WAR-Distribution laden und selbst entpacken (nicht Tomcat ueberlassen,
# sonst wuerde ein spaeteres Redeploy des .war unser BPEL-Deployment loeschen)
RUN mkdir -p /tmp/apache-ode /usr/local/tomcat/webapps/ode \
    && curl -fsSL -o /tmp/apache-ode-war.zip https://archive.apache.org/dist/ode/apache-ode-war-1.3.8.zip \
    && unzip -q /tmp/apache-ode-war.zip -d /tmp/apache-ode \
    && unzip -q /tmp/apache-ode/apache-ode-war-1.3.8/ode.war -d /usr/local/tomcat/webapps/ode \
    && rm -rf /tmp/apache-ode-war.zip /tmp/apache-ode

# BPEL-Deployment (deploy.xml + .bpel + WSDLs); der Verzeichnisname wird von ODE als Deployment-Name verwendet
COPY bestellung-bpel/src/main/bpel /usr/local/tomcat/webapps/ode/WEB-INF/processes/BestellungFreigabeProcess

# CORS-Filter als Web-Fragment-Jar einbinden (fuer die Browser-GUI, falls sie ODE
# je direkt statt nur ueber die REST-Fassade auf WildFly anspricht)
COPY docker/ode/cors-fragment /tmp/cors-fragment
RUN cd /tmp/cors-fragment \
    && jar cf /usr/local/tomcat/webapps/ode/WEB-INF/lib/ode-cors-fragment.jar META-INF/web-fragment.xml \
    && rm -rf /tmp/cors-fragment

EXPOSE 8080
