-- Khởi tạo ENUM
CREATE TYPE trang_thai_gd_enum AS ENUM ('CHO_XAC_NHAN', 'THANH_CONG', 'THAT_BAI', 'HOAN_TIEN');
CREATE TYPE trang_thai_ds_enum AS ENUM ('CHO_XAC_NHAN', 'DANG_XU_LY', 'HOAN_TAT', 'THAT_BAI');
CREATE TYPE trang_thai_ht_enum AS ENUM ('CHO_XAC_NHAN', 'HOAN_TAT', 'TU_CHOI');

-- Bảng Phiếu Đối Soát
CREATE TABLE phieu_doi_soat (
    ma_doi_soat BIGSERIAL PRIMARY KEY,
    ma_tai_khoan BIGINT NOT NULL,
    hoa_hong NUMERIC(5,2) NOT NULL DEFAULT 10.00,
    tong_doanh_thu NUMERIC(15,2) NOT NULL,
    so_tien_thuc_te NUMERIC(15,2) NOT NULL,
    trang_thai trang_thai_ds_enum NOT NULL DEFAULT 'CHO_XAC_NHAN',
    ma_giao_dich_ngan_hang VARCHAR(100),
    ngay_tao TIMESTAMP DEFAULT NOW(),
    ngay_chuyen_thanh_cong TIMESTAMP,
    CONSTRAINT fk_pds_tknh FOREIGN KEY (ma_tai_khoan) REFERENCES tai_khoan_ngan_hang(ma_tai_khoan)
);

-- Bảng Phiếu Mua Khóa Học
CREATE TABLE phieu_mua_khoa_hoc (
    ma_phieu BIGSERIAL PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL,
    ma_khoa_hoc BIGINT NOT NULL,
    loai_giay_phep VARCHAR(20) NOT NULL,
    so_tien NUMERIC(15,2) NOT NULL,
    ma_giao_dich_vnpay VARCHAR(100) UNIQUE NOT NULL,
    trang_thai trang_thai_gd_enum NOT NULL,
    trang_thai_doi_soat VARCHAR(20) NOT NULL DEFAULT 'CHUA_DOI_SOAT',
    ma_doi_soat BIGINT,
    han_giu_tien TIMESTAMP,
    ngay_mua_thanh_cong TIMESTAMP,
    ngay_tao TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_pmkh_nguoi_dung FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT fk_pmkh_khoa_hoc FOREIGN KEY (ma_khoa_hoc) REFERENCES khoa_hoc(ma_khoa_hoc),
    CONSTRAINT fk_pmkh_doi_soat FOREIGN KEY (ma_doi_soat) REFERENCES phieu_doi_soat(ma_doi_soat)
);

-- Bảng Phiếu Trả Phí Lớp Học
CREATE TABLE phieu_tra_phi_lop_hoc (
    ma_phieu BIGSERIAL PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL,
    ma_lop_hoc BIGINT NOT NULL,
    so_tien NUMERIC(15,2) NOT NULL,
    ma_giao_dich_vnpay VARCHAR(100) UNIQUE NOT NULL,
    trang_thai trang_thai_gd_enum NOT NULL,
    trang_thai_doi_soat VARCHAR(20) NOT NULL DEFAULT 'CHUA_DOI_SOAT',
    ma_doi_soat BIGINT,
    han_giu_tien TIMESTAMP,
    ngay_mua_thanh_cong TIMESTAMP,
    ngay_tao TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_ptplh_nguoi_dung FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT fk_ptplh_lop_hoc FOREIGN KEY (ma_lop_hoc) REFERENCES lop_hoc(ma_lop_hoc),
    CONSTRAINT fk_ptplh_doi_soat FOREIGN KEY (ma_doi_soat) REFERENCES phieu_doi_soat(ma_doi_soat)
);

-- Bảng Phiếu Hoàn Tiền Lớp Học
CREATE TABLE phieu_hoan_tien_lop_hoc (
    ma_hoan_tien BIGSERIAL PRIMARY KEY,
    ma_phieu_giao_dich BIGINT NOT NULL,
    so_tien NUMERIC(15,2) NOT NULL,
    ly_do TEXT NOT NULL,
    trang_thai trang_thai_ht_enum NOT NULL DEFAULT 'CHO_XAC_NHAN',
    du_lieu_dong JSONB DEFAULT '{}',
    ngay_tao TIMESTAMP DEFAULT NOW(),
    ngay_xu_ly TIMESTAMP,
    CONSTRAINT fk_phtlh_ptplh FOREIGN KEY (ma_phieu_giao_dich) REFERENCES phieu_tra_phi_lop_hoc(ma_phieu)
);

-- Bảng Phiếu Hoàn Tiền Khóa Học
CREATE TABLE phieu_hoan_tien_khoa_hoc (
    ma_hoan_tien BIGSERIAL PRIMARY KEY,
    ma_phieu_giao_dich BIGINT NOT NULL,
    so_tien NUMERIC(15,2) NOT NULL,
    ly_do TEXT NOT NULL,
    trang_thai trang_thai_ht_enum NOT NULL DEFAULT 'CHO_XAC_NHAN',
    du_lieu_dong JSONB DEFAULT '{}',
    ngay_tao TIMESTAMP DEFAULT NOW(),
    ngay_xu_ly TIMESTAMP,
    CONSTRAINT fk_phtkh_pmkh FOREIGN KEY (ma_phieu_giao_dich) REFERENCES phieu_mua_khoa_hoc(ma_phieu)
);

-- Tạo Index
CREATE INDEX idx_pmkh_nd_kh_tt ON phieu_mua_khoa_hoc(ma_nguoi_dung, ma_khoa_hoc, trang_thai);
CREATE INDEX idx_ptplh_nd_lh_tt ON phieu_tra_phi_lop_hoc(ma_nguoi_dung, ma_lop_hoc, trang_thai);
CREATE INDEX idx_pds_tk_tt ON phieu_doi_soat(ma_tai_khoan, trang_thai);
