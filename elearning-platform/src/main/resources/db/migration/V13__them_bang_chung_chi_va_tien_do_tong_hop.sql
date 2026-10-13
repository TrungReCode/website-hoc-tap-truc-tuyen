-- V13__them_bang_chung_chi_va_tien_do_tong_hop.sql
-- Thêm 4 bảng phục vụ tính năng cấp giấy xác nhận hoàn thành:
--   1. tien_do_khoa_hoc_ca_nhan  — % hoàn thành học liệu gốc (tự học, không qua lớp)
--   2. tien_do_lop_hoc           — % hoàn thành lộ trình lớp (bài gốc + bài bổ sung)
--   3. giay_xac_nhan_ca_nhan     — giấy xác nhận hoàn thành khóa học tự học
--   4. giay_xac_nhan_lop_hoc     — giấy xác nhận hoàn thành lớp học có giảng viên
--
-- KHÔNG sửa bảng tien_do_hoc đã có (ghi theo bài học, dùng làm nguồn tính toán).

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. Tiến độ tổng hợp: Học viên × Khóa học (tự học)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE tien_do_khoa_hoc_ca_nhan (
    ma_tien_do          BIGSERIAL PRIMARY KEY,
    ma_hoc_vien         BIGINT        NOT NULL REFERENCES nguoi_dung(ma_nguoi_dung),
    ma_khoa_hoc         BIGINT        NOT NULL REFERENCES khoa_hoc(ma_khoa_hoc),

    -- Số bài học gốc (loại != QUIZ, không bị ẩn) — snapshot thời điểm cập nhật
    tong_bai_hoc        INT           NOT NULL DEFAULT 0,
    bai_hoan_thanh      INT           NOT NULL DEFAULT 0,

    -- Số quiz gốc + số quiz đã đạt điểm tối thiểu ít nhất 1 lần
    tong_quiz           INT           NOT NULL DEFAULT 0,
    quiz_dat            INT           NOT NULL DEFAULT 0,

    phan_tram_hoan_thanh NUMERIC(5,2) NOT NULL DEFAULT 0,

    -- TRUE khi phan_tram = 100 VÀ quiz_dat = tong_quiz
    da_du_dieu_kien     BOOLEAN       NOT NULL DEFAULT FALSE,

    ngay_cap_nhat       TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_tdkhcn_hv_kh UNIQUE (ma_hoc_vien, ma_khoa_hoc)
);

CREATE INDEX idx_tdkhcn_hv        ON tien_do_khoa_hoc_ca_nhan(ma_hoc_vien);
CREATE INDEX idx_tdkhcn_du_dk     ON tien_do_khoa_hoc_ca_nhan(da_du_dieu_kien) WHERE da_du_dieu_kien = TRUE;

-- ─────────────────────────────────────────────────────────────────────────────
-- 2. Tiến độ tổng hợp: Học viên × Lớp học
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE tien_do_lop_hoc (
    ma_tien_do          BIGSERIAL PRIMARY KEY,
    ma_hoc_vien         BIGINT        NOT NULL REFERENCES nguoi_dung(ma_nguoi_dung),
    ma_lop_hoc          BIGINT        NOT NULL REFERENCES lop_hoc(ma_lop_hoc),

    -- Bài gốc không bị ẩn theo cau_hinh_bai_hoc + bài bổ sung đang hiển thị
    tong_bai_hoc        INT           NOT NULL DEFAULT 0,
    bai_hoan_thanh      INT           NOT NULL DEFAULT 0,

    -- Quiz gốc (không bị ẩn) + quiz bổ sung
    tong_quiz           INT           NOT NULL DEFAULT 0,
    quiz_dat            INT           NOT NULL DEFAULT 0,

    diem_trung_binh     NUMERIC(5,2),

    phan_tram_hoan_thanh NUMERIC(5,2) NOT NULL DEFAULT 0,

    da_du_dieu_kien     BOOLEAN       NOT NULL DEFAULT FALSE,

    ngay_cap_nhat       TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_tdlh_hv_lh UNIQUE (ma_hoc_vien, ma_lop_hoc)
);

CREATE INDEX idx_tdlh_hv      ON tien_do_lop_hoc(ma_hoc_vien);
CREATE INDEX idx_tdlh_du_dk   ON tien_do_lop_hoc(da_du_dieu_kien) WHERE da_du_dieu_kien = TRUE;

-- ─────────────────────────────────────────────────────────────────────────────
-- 3. Giấy xác nhận hoàn thành khóa học (tự học — Giấy phép cá nhân)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE giay_xac_nhan_ca_nhan (
    ma_giay             BIGSERIAL     PRIMARY KEY,

    -- Định danh duy nhất để xác thực trực tuyến (và in lên QR)
    ma_xac_thuc         VARCHAR(50)   UNIQUE NOT NULL,

    ma_hoc_vien         BIGINT        NOT NULL REFERENCES nguoi_dung(ma_nguoi_dung),
    ma_khoa_hoc         BIGINT        NOT NULL REFERENCES khoa_hoc(ma_khoa_hoc),

    phan_tram_hoan_thanh NUMERIC(5,2) NOT NULL,
    diem_trung_binh     NUMERIC(5,2),

    ngay_cap            TIMESTAMP     NOT NULL DEFAULT NOW(),

    -- URL file PDF đã render + upload lên Cloudinary/Firebase (nullable đến khi render xong)
    file_pdf_url        TEXT,
    qr_code_url         TEXT,

    -- HOP_LE | DA_THU_HOI (nếu sau này phát sinh tranh chấp)
    trang_thai          VARCHAR(20)   NOT NULL DEFAULT 'HOP_LE',

    CONSTRAINT uq_gxncn_hv_kh UNIQUE (ma_hoc_vien, ma_khoa_hoc)
);

CREATE INDEX idx_gxncn_hv        ON giay_xac_nhan_ca_nhan(ma_hoc_vien);
CREATE INDEX idx_gxncn_xac_thuc  ON giay_xac_nhan_ca_nhan(ma_xac_thuc);

-- ─────────────────────────────────────────────────────────────────────────────
-- 4. Giấy xác nhận hoàn thành lớp học (có giảng viên phụ trách)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE giay_xac_nhan_lop_hoc (
    ma_giay             BIGSERIAL     PRIMARY KEY,
    ma_xac_thuc         VARCHAR(50)   UNIQUE NOT NULL,

    ma_hoc_vien         BIGINT        NOT NULL REFERENCES nguoi_dung(ma_nguoi_dung),
    ma_lop_hoc          BIGINT        NOT NULL REFERENCES lop_hoc(ma_lop_hoc),

    phan_tram_hoan_thanh NUMERIC(5,2) NOT NULL,
    diem_trung_binh     NUMERIC(5,2),

    ngay_cap            TIMESTAMP     NOT NULL DEFAULT NOW(),

    file_pdf_url        TEXT,
    qr_code_url         TEXT,

    trang_thai          VARCHAR(20)   NOT NULL DEFAULT 'HOP_LE',

    CONSTRAINT uq_gxnlh_hv_lh UNIQUE (ma_hoc_vien, ma_lop_hoc)
);

CREATE INDEX idx_gxnlh_hv        ON giay_xac_nhan_lop_hoc(ma_hoc_vien);
CREATE INDEX idx_gxnlh_xac_thuc  ON giay_xac_nhan_lop_hoc(ma_xac_thuc);
