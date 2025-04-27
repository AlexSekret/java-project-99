# Этап сборки
FROM eclipse-temurin:21-jdk as builder

WORKDIR /app

# Копируем только файлы, необходимые для загрузки зависимостей
COPY build.gradle.kts settings.gradle.kts gradle.properties /app/
COPY gradle /app/gradle

# Загружаем зависимости (кешируем этот слой)
RUN ./gradlew dependencies --no-daemon

# Копируем весь исходный код
COPY src /app/src

# Собираем JAR
RUN ./gradlew bootJar --no-daemon

# Этап запуска
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Копируем JAR из этапа сборки
COPY --from=builder /app/build/libs/*.jar app.jar

# Параметры для оптимизации памяти (настройте под ваше приложение)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=50"

# Порт для HTTP сервера
EXPOSE 8080

# Команда запуска
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
