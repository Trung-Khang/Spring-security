Spring-security_Bài tập lập trình WEB ngày 28 tháng 9 năm 2026

Hai ví dụ Spring Security được triển khai độc lập:

- `example-01-email-login`: đăng nhập bằng email, Thymeleaf fragments thuần.
- `example-02-custom-login`: đăng nhập bằng username hoặc email, custom principal và Layout Dialect.

Mỗi project có README riêng, cấu hình mẫu trong `.env.example`, và test chạy độc lập.

## Chạy và kiểm thử

```powershell
cd example-01-email-login
mvn test
mvn spring-boot:run

cd ../example-02-custom-login
mvn test
mvn spring-boot:run
```

Ví dụ 1 chạy mặc định ở cổng `8088`; Ví dụ 2 ở cổng `8081`. Cả hai dùng H2 in-memory khi chưa tạo `.env`. Để kết nối SQL Server, sao chép `.env.example` thành `.env` và điền thông tin cục bộ; không commit `.env`.

Ví dụ 3 trong tài liệu chỉ là mô tả yêu cầu và cần tài liệu DOCX bổ sung nên chưa triển khai.
