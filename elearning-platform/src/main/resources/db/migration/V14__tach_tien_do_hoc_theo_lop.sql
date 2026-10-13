-- V14__tach_tien_do_hoc_theo_lop.sql
-- BUG NGHIÊM TRỌNG: tien_do_hoc khớp per (ma_hoc_vien, ma_bai_hoc) TOÀN CỤC,
-- dùng chung cho MỌI lớp học mở từ cùng 1 khóa học + luồng tự học. Học viên
-- đã có tiến độ (tự học hoặc ở lớp A) khi tham gia lớp B (dùng chung khóa
-- học gốc) sẽ thấy NGAY tiến độ cũ dù chưa học bất kỳ bài nào trong lớp B.
--
-- Cách sửa: thêm ma_lop_hoc (NULL = tiến độ tự học, NOT NULL = tiến độ
-- riêng của đúng 1 lớp). Dùng UNIQUE INDEX có điều kiện (partial) thay vì
-- UNIQUE constraint thường, vì Postgres coi nhiều NULL là KHÔNG trùng nhau
-- trong UNIQUE constraint gộp — cần ép rõ "chỉ 1 dòng tự học/học viên/bài".

ALTER TABLE tien_do_hoc
    ADD COLUMN ma_lop_hoc BIGINT NULL REFERENCES lop_hoc(ma_lop_hoc);

ALTER TABLE tien_do_hoc DROP CONSTRAINT IF EXISTS tien_do_hoc_ma_hoc_vien_ma_bai_hoc_key;

CREATE UNIQUE INDEX uq_tdh_tu_hoc ON tien_do_hoc (ma_hoc_vien, ma_bai_hoc)
    WHERE ma_lop_hoc IS NULL;

CREATE UNIQUE INDEX uq_tdh_theo_lop ON tien_do_hoc (ma_hoc_vien, ma_bai_hoc, ma_lop_hoc)
    WHERE ma_lop_hoc IS NOT NULL;

CREATE INDEX idx_tdh_lop_hoc ON tien_do_hoc (ma_lop_hoc) WHERE ma_lop_hoc IS NOT NULL;

-- Dữ liệu cũ: TOÀN BỘ dòng đã có trước migration này đều được ghi qua
-- capNhatTienDoBaiGoc (bắt buộc phải là thành viên 1 lớp mới ghi được —
-- capNhatTienDoCaNhan/tự học là tính năng MỚI thêm sau), nên chắc chắn
-- 100% dữ liệu cũ thuộc về đúng 1 lớp cụ thể tại thời điểm ghi, không phải
-- tự học. Gán ma_lop_hoc = lớp đầu tiên (theo ngày tham gia) mà học viên
-- đó đang là thành viên VÀ có dùng đúng khóa học chứa bài học đó — suy luận
-- tốt nhất có thể từ dữ liệu hiện có, không thể khôi phục chính xác 100%
-- lớp nào đã tạo ra từng dòng nếu học viên từng ở nhiều lớp cùng khóa học.
UPDATE tien_do_hoc t
SET ma_lop_hoc = (
    SELECT tv.ma_lop_hoc
    FROM thanh_vien_lop tv
    JOIN lop_hoc l ON l.ma_lop_hoc = tv.ma_lop_hoc
    JOIN bai_hoc bh ON bh.ma_bai_hoc = t.ma_bai_hoc
    JOIN chuong c ON c.ma_chuong = bh.ma_chuong
    WHERE tv.ma_hoc_vien = t.ma_hoc_vien
      AND l.ma_khoa_hoc = c.ma_khoa_hoc
    ORDER BY tv.ngay_tham_gia ASC
    LIMIT 1
)
WHERE t.ma_lop_hoc IS NULL;
