-- Khởi tạo ENUM
CREATE TYPE trang_thai_lop_enum AS ENUM ('SAP_DIEN_RA', 'HOAT_DONG', 'DA_DONG');
CREATE TYPE trang_thai_loi_moi_enum AS ENUM ('CHO_XAC_NHAN', 'DONG_Y', 'TU_CHOI');

-- Bảng Lớp Học
CREATE TABLE lop_hoc (
    ma_lop_hoc BIGSERIAL PRIMARY KEY,
    ma_khoa_hoc BIGINT,
    ma_giang_vien BIGINT NOT NULL,
    ma_lop VARCHAR(20) UNIQUE NOT NULL,
    ngay_bat_dau TIMESTAMP NOT NULL,
    ngay_ket_thuc TIMESTAMP,
    si_so INT,
    trang_thai trang_thai_lop_enum NOT NULL DEFAULT 'SAP_DIEN_RA',
    phi_lop_hoc NUMERIC(15,2) DEFAULT 0,
    cong_khai BOOLEAN DEFAULT TRUE,
    ti_le_hoan_thanh INT NOT NULL DEFAULT 80,
    CONSTRAINT fk_lh_khoa_hoc FOREIGN KEY (ma_khoa_hoc) REFERENCES khoa_hoc(ma_khoa_hoc),
    CONSTRAINT fk_lh_giang_vien FOREIGN KEY (ma_giang_vien) REFERENCES nguoi_dung(ma_nguoi_dung)
);

-- Bảng Lời Mời
CREATE TABLE loi_moi (
    ma_loi_moi BIGSERIAL PRIMARY KEY,
    ma_lop_hoc BIGINT NOT NULL,
    email_hocvien VARCHAR(100) NOT NULL,
    ma_nguoi_moi BIGINT NOT NULL,
    trang_thai trang_thai_loi_moi_enum NOT NULL,
    ngay_tao TIMESTAMP DEFAULT NOW(),
    ngay_het_han TIMESTAMP NOT NULL,
    CONSTRAINT fk_lm_lop_hoc FOREIGN KEY (ma_lop_hoc) REFERENCES lop_hoc(ma_lop_hoc),
    CONSTRAINT fk_lm_nguoi_moi FOREIGN KEY (ma_nguoi_moi) REFERENCES nguoi_dung(ma_nguoi_dung)
);

-- Bảng Quyền Sử Dụng (Course Access)
CREATE TABLE quyen_su_dung (
    ma_nguoi_dung BIGINT NOT NULL,
    ma_khoa_hoc BIGINT NOT NULL,
    loai_giay_phep VARCHAR(20) NOT NULL,
    ngay_bat_dau TIMESTAMP NOT NULL DEFAULT NOW(),
    ngay_het_han TIMESTAMP,
    PRIMARY KEY (ma_nguoi_dung, ma_khoa_hoc),
    CONSTRAINT fk_qsd_nguoi_dung FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT fk_qsd_khoa_hoc FOREIGN KEY (ma_khoa_hoc) REFERENCES khoa_hoc(ma_khoa_hoc)
);

-- Bảng Danh Sách Học Viên (Class Members)
CREATE TABLE thanh_vien_lop (
    ma_hoc_vien BIGINT NOT NULL,
    ma_lop_hoc BIGINT NOT NULL,
    ngay_tham_gia TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (ma_hoc_vien, ma_lop_hoc),
    CONSTRAINT fk_tvl_hoc_vien FOREIGN KEY (ma_hoc_vien) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT fk_tvl_lop_hoc FOREIGN KEY (ma_lop_hoc) REFERENCES lop_hoc(ma_lop_hoc)
);

-- Bảng Đánh Giá Lớp Học
CREATE TABLE danh_gia_lop_hoc (
    ma_danh_gia BIGSERIAL PRIMARY KEY,
    ma_hoc_vien BIGINT NOT NULL,
    ma_lop_hoc BIGINT NOT NULL,
    so_sao INT NOT NULL CHECK (so_sao BETWEEN 1 AND 5),
    nhan_xet TEXT,
    ngay_tao TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_dglh_hoc_vien FOREIGN KEY (ma_hoc_vien) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT fk_dglh_lop_hoc FOREIGN KEY (ma_lop_hoc) REFERENCES lop_hoc(ma_lop_hoc),
    UNIQUE (ma_hoc_vien, ma_lop_hoc)
);

-- Tạo Index
CREATE INDEX idx_lh_kh_ml_tt ON lop_hoc(ma_khoa_hoc, ma_lop, trang_thai);
CREATE INDEX idx_lm_lh_tt ON loi_moi(ma_lop_hoc, trang_thai);
