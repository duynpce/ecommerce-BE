# syntax=docker/dockerfile:1
# ^ Required to enable BuildKit cache mounts (--mount=type=cache)

# =============================================================================
# Stage 1 — Compile & package
# =============================================================================
FROM public.ecr.aws/docker/library/maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml .
COPY src ./src

# --mount=type=cache persists ~/.m2 across ALL builds on this machine/runner,
# completely independent of layer cache. Even when pom.xml changes, only
# net-new artifacts are downloaded — existing ones are served from the mount.
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn package -DskipTests -q

# =============================================================================
# Stage 2 — Extract Spring Boot layers
# =============================================================================
FROM public.ecr.aws/docker/library/eclipse-temurin:21-jre-alpine AS extractor
WORKDIR /extract

COPY --from=builder /build/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# =============================================================================
# Stage 3 — Minimal runtime image
# =============================================================================
FROM public.ecr.aws/docker/library/eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.title="user-service" \
      org.opencontainers.image.base.name="eclipse-temurin:21-jre-alpine"

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=extractor --chown=spring:spring /extract/dependencies/          ./
COPY --from=extractor --chown=spring:spring /extract/spring-boot-loader/    ./
COPY --from=extractor --chown=spring:spring /extract/snapshot-dependencies/ ./
COPY --from=extractor --chown=spring:spring /extract/application/           ./

USER spring

EXPOSE 8082

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]