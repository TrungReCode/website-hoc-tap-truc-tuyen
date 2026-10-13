-- NÂNG CẤP: tìm kiếm khóa học trước đây chỉ LIKE '%từ khóa%' trên đúng cột
-- tieu_de (xem KhoaHocKho.timKiemCongKhai cũ) — không xếp hạng độ liên quan,
-- không tìm được trong mieu_ta. Cột sinh tự động dưới đây gộp tieu_de (trọng
-- số A — ưu tiên cao nhất) và mieu_ta (trọng số B) thành 1 tsvector, Postgres
-- tự cập nhật lại mỗi khi tieu_de/mieu_ta đổi (GENERATED ALWAYS ... STORED),
-- không cần trigger thủ công.
--
-- Dùng cấu hình 'simple' (không phải 'english'): Postgres không có sẵn từ
-- điển tiếng Việt, 'simple' chỉ tách từ + hạ chữ thường, không chia động từ/
-- số nhiều như tiếng Anh — chấp nhận được cho tiếng Việt vì tiếng Việt vốn
-- không chia hình thái từ như tiếng Anh, nhưng vẫn cần gõ đúng dạng từ đã
-- lưu (không tự nhận "học" khớp với "họcs" kiểu stemming tiếng Anh).
ALTER TABLE khoa_hoc ADD COLUMN tsv_tim_kiem tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(tieu_de, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(mieu_ta, '')), 'B')
    ) STORED;

CREATE INDEX idx_khoa_hoc_tsv_tim_kiem ON khoa_hoc USING GIN (tsv_tim_kiem);
