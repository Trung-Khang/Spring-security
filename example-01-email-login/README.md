# Example 01 - Email login

Chạy với `mvn spring-boot:run`. Mặc định ứng dụng dùng H2 in-memory; để dùng SQL Server, tạo file `.env` từ `.env.example` và cấu hình `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER=com.microsoft.sqlserver.jdbc.SQLServerDriver`.

Test: `mvn test`. Trang đăng nhập: `http://localhost:8088/login`.
