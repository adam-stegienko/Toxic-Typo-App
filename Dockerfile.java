FROM maven:3.6.2-jdk-8
COPY ./toxictypoapp /usr/app
WORKDIR /usr/app
RUN mvn clean verify
WORKDIR /usr/app/target
RUN echo "#!/bin/bash \n java -jar toxictypoapp-1.0-SNAPSHOT.jar" > ./entry-point.sh
RUN chmod +x ./entry-point.sh
ENTRYPOINT ["./entry-point.sh"]