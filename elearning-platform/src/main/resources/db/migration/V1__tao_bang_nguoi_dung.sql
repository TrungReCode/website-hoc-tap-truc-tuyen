-- Khởi tạo ENUM cho phân hệ Người Dùng
CREATE TYPE vai_tro_enum AS ENUM ('QTV_CAO_CAP', 'QTV_NOI_DUNG', 'QTV_TAI_CHINH', 'NGUOI_DUNG');
CREATE TYPE trang_thai_nd_enum AS ENUM ('HOAT_DONG', 'BI_CAM', 'CHO_XAC_NHAN');

-- Bảng Người Dùng
CREATE TABLE nguoi_dung (
    ma_nguoi_dung BIGSERIAL PRIMARY KEY,
    ho_ten VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    sdt VARCHAR(15),
    mat_khau VARCHAR(255) NOT NULL,
    vai_tro vai_tro_enum NOT NULL DEFAULT 'NGUOI_DUNG',
    trang_thai trang_thai_nd_enum NOT NULL,
    anh_dai_dien TEXT,
    tieu_su TEXT,
    ngay_tao TIMESTAMP DEFAULT NOW()
);

-- Bảng Mã Làm Mới Phiên (Refresh Tokens)
CREATE TABLE refresh_tokens (
    ma BIGSERIAL PRIMARY KEY,
    nguoi_dung_id BIGINT NOT NULL,
    token VARCHAR(512) UNIQUE NOT NULL,
    het_han TIMESTAMP NOT NULL,
    thu_hoi BOOLEAN DEFAULT FALSE,
    ngay_tao TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_rt_nguoi_dung FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung(ma_nguoi_dung)
);

-- Bảng Tài Khoản Ngân Hàng (Thuộc Module Định Danh)
CREATE TABLE tai_khoan_ngan_hang (
    ma_tai_khoan BIGSERIAL PRIMARY KEY,
    ma_giang_vien BIGINT NOT NULL,
    ten_chu_tai_khoan VARCHAR(100) NOT NULL,
    so_tai_khoan VARCHAR(30) NOT NULL,
    ten_ngan_hang VARCHAR(100) NOT NULL,
    mac_dinh BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_tknh_nguoi_dung FOREIGN KEY (ma_giang_vien) REFERENCES nguoi_dung(ma_nguoi_dung)
);

-- Tạo Index theo yêu cầu tối ưu hiệu năng
CREATE UNIQUE INDEX idx_nguoi_dung_email ON nguoi_dung(email);
CREATE INDEX idx_rt_nguoidung_token ON refresh_tokens(nguoi_dung_id, token);
