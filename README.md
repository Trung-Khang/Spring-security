# Spring-security_Bài tập lập trình WEB ngày 28 tháng 9 năm 2026

Hai ví dụ Spring Security được triển khai độc lập:

- `example-01-email-login`: đăng nhập bằng email, Thymeleaf fragments thuần.
- `example-02-custom-login`: đăng nhập bằng username hoặc email, custom principal và Layout Dialect.
- `example-03-full-security-shop`: đăng ký OTP, session login, quản lý user/product, phân quyền và upload Cloudinary.

Mỗi project có README riêng, cấu hình mẫu trong `.env.example`, và test chạy độc lập.

## Chạy và kiểm thử

```powershell
cd example-01-email-login
mvn clean test
mvn spring-boot:run

cd ../example-02-custom-login
mvn clean test
mvn spring-boot:run

cd ../example-03-full-security-shop
mvn clean verify
mvn spring-boot:run
```

Ví dụ 1 chạy ở cổng `8088`, Ví dụ 2 ở cổng `8081`, Ví dụ 3 ở cổng `8080`. Cả ba đóng gói H2 ở runtime và dùng H2 in-memory khi chưa tạo `.env`. Để kết nối SQL Server, sao chép `.env.example` của project thành `.env`, đặt `DB_DRIVER=com.microsoft.sqlserver.jdbc.SQLServerDriver` và `DDL_AUTO=update`; không commit `.env`.

Ví dụ 3 yêu cầu cấu hình Mail để gửi OTP và Cloudinary để upload ảnh thật. Hướng dẫn chi tiết nằm trong [README của Ví dụ 3](example-03-full-security-shop/README.md).
