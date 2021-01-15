#!/usr/bin/env bash

ROOT_DIR=$(cd $(dirname $0) && pwd)
APP_NAME=bookmark-ingest-job
PROJECT_ID=${GCLOUD_PROJECT}
TAG_NAME=${1:-$(date +%s)}
IMAGE_TAG="production${GITHUB_SHA:-}"
GCR_IMAGE_NAME=gcr.io/${PROJECT_ID}/twi-${APP_NAME}
mvn -f ${ROOT_DIR}/../../pom.xml -DskipTests=true \
  -e -Dspring.profiles.active=production  \
  clean \
  verify \
  deploy \
  spring-boot:build-image

image_id=$(docker images -q $APP_NAME)
docker tag "${image_id}" ${GCR_IMAGE_NAME}:latest
docker tag "${image_id}" ${GCR_IMAGE_NAME}:${IMAGE_TAG}
docker push ${GCR_IMAGE_NAME}:latest
docker push ${GCR_IMAGE_NAME}:${IMAGE_TAG}



#
## the build reads datasource configuration from a property file by default.
### that's if the default profile is active. Here, we run the build with the `ci` profile,
### which does nothing to influence the properties. We must provide them at runtime
### using environment varaibles specified as secrets in Github.
#
#mvn -Pci \
#  -Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
#  -Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
#  -Dspring.datasource.url=${SPRING_DATASOURCE_URL} \
#  spring-boot:run
