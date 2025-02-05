# Stage 1: Build the application. Use official eclipse-temurin image with Maven
FROM eclipse-temurin:21-jdk-alpine AS build

# Define constants/arguments for reuse
ARG APP_NAME=currency-account-0.0.1-SNAPSHOT.jar
WORKDIR /app

# Install Maven and prepare for build
RUN apk add --no-cache maven \
 && mkdir -p /app

# Copy dependencies and the source code
COPY pom.xml .
COPY src /app/src

# Get all Maven dependencies offline (this will speed up subsequent builds)
RUN mvn dependency:go-offline

# Copy source code and build (flag can be removed -DskipTests. Added to improve build time)
RUN mvn clean package -DskipTests

# Check the contents of the target folder
RUN ls -la /app/target

# Stage 2: Create the lightweight runtime image
FROM eclipse-temurin:21-jdk-alpine

# Copy the built application from Stage 1
COPY --from=build /app/target/${APP_NAME} /app/${APP_NAME}

# Set default command
CMD ["java", "-jar", "/currency-account-0.0.1-SNAPSHOT.jar"]
