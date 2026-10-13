-- V12: Bổ sung cột kiểm duyệt cho bảng danh_gia_khoa_hoc
--
-- Lý do: UC-11 (Kiểm duyệt nội dung) cho phép Quản trị viên nội dung ẩn
-- tạm thời (không xóa) các bài đánh giá vi phạm tiêu chuẩn cộng đồng.
-- Cột "an" đánh dấu đánh giá bị ẩn khỏi danh sách công khai và khỏi điểm
-- trung bình khóa học; "ly_do_an" lưu lý do QTV cung cấp để gửi thông báo
-- lại cho học viên bị ảnh hưởng.
ALTER TABLE danh_gia_khoa_hoc
    ADD COLUMN an BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN ly_do_an TEXT;
