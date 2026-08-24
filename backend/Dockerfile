FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
# Fail the image build if duplicate Flyway versions are ever packaged.
RUN dups="$(find src/main/resources/db/migration -maxdepth 1 -type f -name 'V*.sql' -printf '%f\n' | sed -n 's/^V\([0-9][0-9]*\)__.*/\1/p' | sort | uniq -d)"; \
    if [ -n "$dups" ]; then echo "ERROR: duplicate Flyway migration version(s): $dups"; exit 1; fi
RUN mvn -q clean -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /workspace/target/cetaring-backend-*.jar /app/cetaring-backend.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/cetaring-backend.jar"]
