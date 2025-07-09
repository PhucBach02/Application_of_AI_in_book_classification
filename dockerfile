# Sử dụng OpenJDK nhẹ để giảm kích thước image
FROM openjdk:17-jdk-slim

# Đặt thư mục làm việc trong container
WORKDIR /app

# Copy file JAR vào container
COPY build/libs/DoAnCuoiKi-1.0-SNAPSHOT.jar app.jar

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
