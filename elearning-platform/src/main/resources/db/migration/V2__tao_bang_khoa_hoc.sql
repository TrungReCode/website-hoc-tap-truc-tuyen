-- Khởi tạo ENUM
CREATE TYPE trang_thai_kh_enum AS ENUM ('NHAP', 'CHO_KIEM_DUYET', 'CONG_KHAI', 'LUU_TRU', 'NOI_BO');

-- Bảng Danh Mục
CREATE TABLE danh_muc (
    ma_danh_muc BIGSERIAL PRIMARY KEY,
    ten_danh_muc VARCHAR(100) UNIQUE NOT NULL,
    muc_cha BIGINT,
    CONSTRAINT fk_dm_dm FOREIGN KEY (muc_cha) REFERENCES danh_muc(ma_danh_muc)
);

-- Bảng Khóa Học
CREATE TABLE khoa_hoc (
    ma_khoa_hoc BIGSERIAL PRIMARY KEY,
    ma_tac_gia BIGINT NOT NULL,
    ma_danh_muc BIGINT NOT NULL,
    tieu_de VARCHAR(255) NOT NULL,
    mieu_ta TEXT,
    anh_bia TEXT,
    gia NUMERIC(15,2) NOT NULL DEFAULT 0,
    phi_day_hoc NUMERIC(15,2),
    trang_thai trang_thai_kh_enum NOT NULL DEFAULT 'NHAP',
    ly_do_tu_choi TEXT,
    ngay_tao TIMESTAMP DEFAULT NOW(),
    ngay_cap_nhat TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_kh_nguoi_dung FOREIGN KEY (ma_tac_gia) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT fk_kh_danh_muc FOREIGN KEY (ma_danh_muc) REFERENCES danh_muc(ma_danh_muc)
);

-- Bảng Chương
CREATE TABLE chuong (
    ma_chuong BIGSERIAL PRIMARY KEY,
    ma_khoa_hoc BIGINT NOT NULL,
    tieu_de VARCHAR(255) NOT NULL,
    vi_tri INT NOT NULL,
    CONSTRAINT fk_chuong_khoa_hoc FOREIGN KEY (ma_khoa_hoc) REFERENCES khoa_hoc(ma_khoa_hoc)
);

-- Bảng Bài Học (Lessons)
CREATE TABLE bai_hoc (
    ma_bai_hoc BIGSERIAL PRIMARY KEY,
    ma_chuong BIGINT NOT NULL,
    tieu_de VARCHAR(255) NOT NULL,
    loai VARCHAR(20) NOT NULL,
    vi_tri INT NOT NULL,
    duong_dan TEXT NOT NULL,
    duoi_tep VARCHAR(10),
    metadata JSONB,
    xem_thu BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_bh_chuong FOREIGN KEY (ma_chuong) REFERENCES chuong(ma_chuong)
);

-- Bảng Bài Kiểm Tra
CREATE TABLE bai_kiem_tra (
    ma_bai_kiem_tra BIGSERIAL PRIMARY KEY,
    ma_bai_hoc BIGINT UNIQUE NOT NULL,
    diem_toi_thieu INT NOT NULL DEFAULT 70,
    thoi_gian INT,
    CONSTRAINT fk_bkt_bai_hoc FOREIGN KEY (ma_bai_hoc) REFERENCES bai_hoc(ma_bai_hoc)
);

-- Bảng Câu Hỏi
CREATE TABLE cau_hoi (
    ma_cau_hoi BIGSERIAL PRIMARY KEY,
    ma_bai_kiem_tra BIGINT NOT NULL,
    noi_dung TEXT NOT NULL,
    vi_tri INT NOT NULL,
    loai_cau_hoi VARCHAR(20) NOT NULL DEFAULT 'MOT_DAP_AN',
    dap_an JSONB NOT NULL,
    CONSTRAINT fk_ch_bkt FOREIGN KEY (ma_bai_kiem_tra) REFERENCES bai_kiem_tra(ma_bai_kiem_tra)
);

-- Bảng Đánh Giá Khóa Học
CREATE TABLE danh_gia_khoa_hoc (
    ma_danh_gia BIGSERIAL PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL,
    ma_khoa_hoc BIGINT NOT NULL,
    so_sao INT NOT NULL CHECK (so_sao BETWEEN 1 AND 5),
    nhan_xet TEXT,
    ngay_tao TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_dgkh_nguoi_dung FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT fk_dgkh_khoa_hoc FOREIGN KEY (ma_khoa_hoc) REFERENCES khoa_hoc(ma_khoa_hoc),
    UNIQUE (ma_nguoi_dung, ma_khoa_hoc)
);

-- Tạo Index
CREATE INDEX idx_kh_tg_tt_dm ON khoa_hoc(ma_tac_gia, trang_thai, ma_danh_muc);
