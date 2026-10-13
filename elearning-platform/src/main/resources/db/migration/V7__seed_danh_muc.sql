-- V7: Seed dữ liệu danh mục khóa học ban đầu
-- Khớp đúng 4 danh mục xuất hiện ở sidebar Chợ Khóa Học (màn hình S7 trong 36-manhinh-giaodien.html)
-- Idempotent qua ON CONFLICT để chạy lại an toàn trên môi trường dev đã có dữ liệu.

INSERT INTO danh_muc (ten_danh_muc, muc_cha) VALUES
    ('Lập trình', NULL),
    ('Thiết kế UI/UX', NULL),
    ('Marketing', NULL),
    ('Kinh doanh', NULL)
ON CONFLICT (ten_danh_muc) DO NOTHING;
