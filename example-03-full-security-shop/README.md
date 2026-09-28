# Example 03 - Full Security Shop

Ứng dụng Spring Boot 4.1.1 và Spring Security 7.1 dùng session, Thymeleaf, JPA, MapStruct và BCrypt.

## Chức năng

- Đăng ký tài khoản và xác nhận email bằng OTP 6 chữ số.
- Đăng nhập bằng username hoặc email.
- Gửi lại OTP đăng ký, quên mật khẩu và đặt lại mật khẩu bằng OTP.
- Phân quyền `ROLE_USER` và `ROLE_ADMIN`.
- Quản lý user và product, tìm kiếm, phân trang và đếm số product theo user.
- Product thuộc về user tạo ra. ROLE_USER chỉ thao tác với product của mình; ROLE_ADMIN có thể quản lý toàn bộ product.
- Upload, thay và xóa ảnh qua Cloudinary.
- CSRF bật cho các form thay đổi dữ liệu.

## Yêu cầu

- JDK 24 hoặc tương thích.
- Maven 3.9+.
- SQL Server, Gmail SMTP và Cloudinary chỉ cần cấu hình nếu muốn dùng các dịch vụ đó. H2 chạy mặc định.

## Chạy với H2

```powershell
mvn clean verify
mvn spring-boot:run
```

Mở [http://localhost:8080](http://localhost:8080), `/login` hoặc `/register`. Tài khoản quản trị mặc định dùng username `admin`, email `admin@example.com`, mật khẩu `change-me`. Hãy đặt `ADMIN_PASSWORD` riêng trong `.env` trước khi sử dụng ngoài môi trường demo.

## Cấu hình SQL Server, Mail và Cloudinary

Sao chép `.env.example` thành `.env` trong thư mục project. Ứng dụng tự đọc `.env` khi chạy từ thư mục project.

SQL Server: tạo database `webst3`, rồi điền `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER=com.microsoft.sqlserver.jdbc.SQLServerDriver` và `DDL_AUTO=update`.

Mail: đặt `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME` và Gmail app password vào `MAIL_PASSWORD`. Không dùng mật khẩu đăng nhập Gmail thông thường.

Cloudinary: điền `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY` và `CLOUDINARY_API_SECRET`. Nếu chưa cấu hình, ứng dụng vẫn khởi động bằng H2 nhưng thao tác upload ảnh trả thông báo cần cấu hình Cloudinary.

Không commit `.env`; chỉ commit `.env.example` với giá trị minh họa.

## Kiểm thử và đóng gói

```powershell
mvn clean test
mvn clean verify
mvn clean package
java -jar target/example-03-full-security-shop-1.0.0.jar
```

Các test chạy bằng H2. Mail và Cloudinary được mock, không gửi email hoặc upload tài nguyên thật.

## Quyền truy cập

- Người dùng đã xác nhận email có thể đăng nhập và quản lý sản phẩm của mình.
- ROLE_ADMIN truy cập `/dashboard` và `/users/**`, đồng thời có thể quản lý mọi product.
- OTP hết hạn sau 5 phút, giới hạn 5 lần nhập sai, được lưu dưới dạng BCrypt và chỉ dùng một lần.

## Giới hạn dịch vụ

Gửi OTP thật yêu cầu SMTP hoạt động. Upload ảnh thật yêu cầu tài khoản Cloudinary. Khi chạy mặc định bằng H2 mà chưa điền Mail, ứng dụng vẫn khởi động nhưng gửi OTP sẽ báo lỗi cấu hình Mail.
