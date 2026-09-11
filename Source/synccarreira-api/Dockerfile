# Dockerfile — arquivo novo, necessário pro deploy no Render.
# Render não tem runtime nativo pra JVM (só Node/Python/Ruby/Go/Rust/Elixir);
# qualquer coisa em cima de Java exige Docker:
# https://render.com/docs/native-runtimes
#
# Não altera nenhum arquivo Java/Gradle existente. Só empacota o build e o
# start que já funcionam localmente (./gradlew build -x test + java -jar).

# ---- build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copia primeiro só o necessário pra resolver dependências (cache de camada)
COPY gradlew gradlew.bat settings.gradle build.gradle lombok.config ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew build -x test --no-daemon

# ---- runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# O Render injeta a variável PORT com a porta que ele espera que a app escute;
# não há server.port fixo em nenhum properties existente (default do Spring é
# 8080), então repassamos PORT pro Spring via argumento de linha de comando —
# sem precisar editar nenhum arquivo de configuração.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8080}"]
