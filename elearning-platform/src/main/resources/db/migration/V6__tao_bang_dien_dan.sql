-- Bảng Bài Đăng
CREATE TABLE bai_dang (
    ma_bai_dang BIGSERIAL PRIMARY KEY,
    ma_lop_hoc BIGINT NOT NULL,
    ma_nguoi_dang BIGINT NOT NULL,
    loai VARCHAR(20) NOT NULL,
    tieu_de VARCHAR(255) NOT NULL,
    noi_dung TEXT,
    tep_dinh_kem JSONB DEFAULT '[]',
    giai_dap BOOLEAN DEFAULT FALSE,
    ngay_tao TIMESTAMP DEFAULT NOW(),
    xoa_mem BOOLEAN DEFAULT FALSE,
    ngay_cap_nhat TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_bd_lop_hoc FOREIGN KEY (ma_lop_hoc) REFERENCES lop_hoc(ma_lop_hoc),
    CONSTRAINT fk_bd_nguoi_dang FOREIGN KEY (ma_nguoi_dang) REFERENCES nguoi_dung(ma_nguoi_dung)
);

-- Bảng Bình Luận
CREATE TABLE binh_luan (
    ma_binh_luan BIGSERIAL PRIMARY KEY,
    ma_bai_dang BIGINT NOT NULL,
    ma_nguoi_dung BIGINT NOT NULL,
    ma_cha BIGINT,
    noi_dung TEXT NOT NULL,
    ngay_tao TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_bl_bai_dang FOREIGN KEY (ma_bai_dang) REFERENCES bai_dang(ma_bai_dang),
    CONSTRAINT fk_bl_nguoi_dung FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT fk_bl_cha FOREIGN KEY (ma_cha) REFERENCES binh_luan(ma_binh_luan)
);

-- Bảng Thông Báo
CREATE TABLE thong_bao (
    ma_thong_bao BIGSERIAL PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL,
    loai VARCHAR(50) NOT NULL,
    tieu_de VARCHAR(255) NOT NULL,
    noi_dung TEXT,
    trang_thai_doc BOOLEAN DEFAULT FALSE,
    thong_bao_tuong_tac BOOLEAN DEFAULT FALSE,
    ngay_tao TIMESTAMP DEFAULT NOW(),
    du_lieu_dong JSONB DEFAULT '{}',
    CONSTRAINT fk_tb_nguoi_dung FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
);

-- Tạo Index
CREATE INDEX idx_bd_lh_nt ON bai_dang(ma_lop_hoc, ngay_tao);
CREATE INDEX idx_bl_bd_nt ON binh_luan(ma_bai_dang, ngay_tao);
CREATE INDEX idx_tb_nd_tt_nt ON thong_bao(ma_nguoi_dung, trang_thai_doc, ngay_tao);
