# 🛠 STAGE 1: Build gst-meet từ mã nguồn Pie-D
FROM docker.io/library/alpine:3.20.0 AS builder

# Cài đặt dependencies cần thiết cho Rust và GStreamer
RUN apk --no-cache --update upgrade --ignore alpine-baselayout \
 && apk --no-cache add \
    build-base \
    gstreamer-dev gst-plugins-base-dev \
    libnice-dev openssl-dev \
    cargo cmake clang16-libclang rust-bindgen \
    git

# Clone gst-meet từ GitHub của Pie-D
WORKDIR /app
RUN git clone https://github.com/Pie-D/gst-meet.git
WORKDIR /app/gst-meet

# Build gst-meet với Cargo
RUN cargo build --release -p gst-meet

# 🛠 STAGE 2: Build API Java
FROM eclipse-temurin:17-jdk-alpine AS api-builder

# Cài đặt Maven
RUN apk --no-cache add maven

# Copy mã nguồn API vào container
WORKDIR /api
COPY . /api

# Build API
RUN mvn clean package -DskipTests

# 🛠 STAGE 3: Tạo image final
FROM docker.io/library/alpine:3.20.0

# Cài đặt runtime dependencies
RUN apk --update --no-cache upgrade --ignore alpine-baselayout \
 && apk --no-cache add \
    openssl \
    gstreamer gst-plugins-good gst-plugins-bad gst-plugins-ugly gst-libav \
    libnice libnice-gstreamer \
    openjdk17-jre

# Copy gst-meet từ builder stage
COPY --from=builder /app/gst-meet/target/release/gst-meet /usr/local/bin

# Copy API từ api-builder stage
COPY --from=api-builder /api/target/gst-meet-api.jar /usr/local/bin/gst-meet-api.jar

# Copy entrypoint script
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Mở cổng API và WebSocket
EXPOSE 8080

# Chạy entrypoint script
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
