FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests
RUN cp target/*.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]