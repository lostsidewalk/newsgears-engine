FROM amazoncorretto:11-alpine-jdk
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENV SPRING_DATASOURCE_URL jdbc:postgresql://feedgears-db01:5432/postgres
ENV SPRING_DATASOURCE_USERNAME postgres
ENV SPRING_DATASOURCE_PASSWORD postgres
ENV SPRING_SQL_INIT_MODE never
ENV NEWS_API_KEY 423c71cf6bd14697ab64d848f796e127
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:55005", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
