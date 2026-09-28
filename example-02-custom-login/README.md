# Example 02 - Custom username/email login

Chạy bằng `mvn spring-boot:run`; mặc định dùng H2 in-memory và chạy tại cổng 8081. Tài khoản mẫu: `user01` hoặc `user01@example.com`, mật khẩu `123456`. Để dùng SQL Server, tạo `.env` từ `.env.example`, giữ `DB_DRIVER=com.microsoft.sqlserver.jdbc.SQLServerDriver` và `DDL_AUTO=update`; không commit `.env`.

Test: `mvn clean test`. Build JAR: `mvn clean package`, sau đó chạy `java -jar target/example-02-custom-login-1.0.0.jar`.
