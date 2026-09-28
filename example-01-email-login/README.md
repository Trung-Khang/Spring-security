# Example 01 - Email login

Chạy với `mvn spring-boot:run`. Mặc định ứng dụng dùng H2 in-memory tại cổng 8088. Để dùng SQL Server, tạo `.env` từ `.env.example`, đặt `DB_DRIVER=com.microsoft.sqlserver.jdbc.SQLServerDriver` và `DDL_AUTO=update`. Không commit file `.env`.

Test: `mvn clean test`. Build JAR: `mvn clean package`, sau đó chạy `java -jar target/example-01-email-login-1.0.0.jar`. Trang đăng nhập: `http://localhost:8088/login`.
