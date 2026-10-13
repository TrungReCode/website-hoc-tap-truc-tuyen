# EduHub VN — Nền tảng học trực tuyến

Hệ thống e-learning full-stack: khóa học tự học + lớp học có giảng viên, thanh toán VNPay, diễn đàn, quiz, cấp chứng chỉ PDF có mã xác thực QR. Ban đầu là đồ án tốt nghiệp, đã qua một đợt rà soát bảo mật/hiệu năng và refactor hạ tầng lưu trữ sau khi bảo vệ.

**Demo:** <!-- TODO: dán link deploy thật vào đây --> | **Video demo 2 phút:** <!-- TODO -->

## Tech stack

**Backend:** Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA, PostgreSQL, Redis, Flyway
**Frontend:** React, Vite
**Hạ tầng ngoài:** VNPay (thanh toán), Cloudinary (video bài giảng), Cloudflare R2 (tài liệu/đính kèm/chứng chỉ), iText (render PDF)

## Kiến trúc

Modular monolith — 5 module nghiệp vụ độc lập (`dinhDanhPhanQuyen`, `choKhoaHoc`, `lopHocLMS`, `thanhToan`, `traoDoiTrongLop`, `chungChi`) cộng 1 module dùng chung (`dungChung`), mỗi module theo 4 lớp `giaoDien → ungDung → haTang → mienDomain`. Mọi lời gọi ra dịch vụ bên ngoài (storage, email, VNPay) đều đi qua đúng 1 Adapter — đổi nhà cung cấp chỉ cần sửa 1 file (minh chứng: toàn bộ việc chuyển từ Firebase Storage sang Cloudflare R2 chỉ động vào 5 Adapter, không đụng tới tầng service).

## Những quyết định kỹ thuật đáng chú ý

Phần này liệt kê các chỗ đã xử lý vượt ra ngoài CRUD thông thường — nơi đáng xem nhất nếu bạn review code cho vị trí backend.

- **Khóa kép khi ghi danh lớp học** (`GhiDanhDichVuImpl`) — một học viên chỉ được vào lớp có phí nếu đã sở hữu khóa học gốc VÀ đã đóng phí lớp. Luồng tham gia miễn phí và luồng thanh toán (VNPay) được viết độc lập ở 2 module khác nhau; phát hiện và vá lỗi luồng thanh toán bỏ sót điều kiện đầu, cho phép lách qua việc mua khóa học gốc — xem `ThanhToanDichVuImpl.taoDonPhiLopHoc()`.
- **Idempotency + pessimistic lock cho thanh toán VNPay** — IPN có thể được gọi nhiều lần cho cùng 1 giao dịch (theo đúng cơ chế VNPay); dùng `@Lock(PESSIMISTIC_WRITE)` khi tra cứu phiếu giao dịch để tránh 2 request IPN/return xử lý trùng cùng lúc, cộng thêm kiểm tra đối chiếu số tiền trước khi ghi nhận thành công.
- **Refresh token rotation + reuse detection** (`XacThucDichVuImpl.lamMoiToken`) — mỗi lần làm mới access token, refresh token cũ bị thu hồi và phát hành token mới. Nếu một token **đã bị thu hồi** lại được dùng lần nữa (dấu hiệu token bị đánh cắp), hệ thống tự động thu hồi toàn bộ phiên đăng nhập của người dùng đó trên mọi thiết bị.
- **Auth qua cookie HttpOnly** thay vì lưu token ở localStorage — giảm thiệt hại nếu có XSS. SameSite=Lax đóng vai trò chống CSRF, có ghi chú rõ trong code về giới hạn (chỉ hoạt động cho frontend/backend cùng site) và hướng xử lý nếu triển khai domain tách biệt.
- **Chống leo thang đặc quyền ở tầng quản trị** (`QuanTriDichVuImpl`) — không ai tự khóa/tự thu hồi quyền chính mình, không tạo được tài khoản admin cấp cao nhất qua API tạo tài khoản thường, mọi thao tác nhạy cảm đều ghi nhật ký.
- **Hạn ngạch thời gian làm bài chống gian lận client-side** (`KiemTraTrongLopDichVuImpl`) — mốc thời gian bắt đầu làm quiz lưu ở Redis phía server, không tin bất kỳ giá trị thời gian nào gửi từ client.

## Chạy thử local

```bash
docker compose up -d        # Postgres + Redis
cd elearning-platform && mvn spring-boot:run
cd elearning-platform-frontend && npm install && npm run dev
```

Cần cấu hình biến môi trường cho VNPay (sandbox), Cloudinary, và Cloudflare R2 — xem `elearning-platform/src/main/resources/application.yml` (profile `dev`) để biết danh sách đầy đủ và giá trị mặc định dùng được ngay cho sandbox/test.

## Trạng thái dự án

Dự án đã hoàn thành phần đồ án tốt nghiệp. Đang trong quá trình bổ sung test tự động và CI cho mục đích portfolio — xem [Issues](../../issues) <!-- TODO: xóa dòng này nếu không dùng GitHub Issues --> để biết việc còn lại.
