FROM docker.artifactory.us.caas.oneadp.com/innerspace/java:17-jre-chainguard-onbuild-amd64
LABEL maintainer=bpm.india@adp.com

# We're using a slightly different healthcheck URL. We'll eventually standardize this.
ENV HEALTHCHECK_URL http://localhost:8080/actuator/health