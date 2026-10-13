-- Chuẩn hóa email của các tài khoản đã tồn tại để khớp với quy tắc
-- chuẩn hóa ở tầng service (trim và chuyển về chữ thường).
--
-- Nếu đang tồn tại hai email chỉ khác nhau ở hoa/thường, UPDATE sẽ vi phạm
-- unique index idx_nguoi_dung_email và migration thất bại rõ ràng. Khi đó cần
-- xử lý thủ công (đổi email hoặc gộp tài khoản) trước khi chạy lại migration.
UPDATE nguoi_dung
SET email = LOWER(TRIM(email))
WHERE email <> LOWER(TRIM(email));