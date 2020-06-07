#!/bin/bash

## the build reads datasource configuration from a property file by default.
## that's if the default profile is active. Here, we run the build with the `ci` profile,
## which does nothing to influence the properties. We must provide them at runtime
## using environment varaibles specified as secrets in Github.

mvn -Pci \
  -Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
  -Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
  -Dspring.datasource.url=${SPRING_DATASOURCE_URL} \
  spring-boot:run
