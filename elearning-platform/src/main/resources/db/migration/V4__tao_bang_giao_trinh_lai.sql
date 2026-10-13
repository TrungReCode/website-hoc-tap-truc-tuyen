-- Bảng Cấu Hình Bài Học (Gốc)
CREATE TABLE cau_hinh_bai_hoc (
    ma_cau_hinh BIGSERIAL PRIMARY KEY,
    ma_lop_hoc BIGINT NOT NULL,
    ma_bai_hoc BIGINT NOT NULL,
    an_bai_hoc BOOLEAN NOT NULL DEFAULT FALSE,
    vi_tri_moi FLOAT,
    ngay_cap_nhat TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_chbh_lop_hoc FOREIGN KEY (ma_lop_hoc) REFERENCES lop_hoc(ma_lop_hoc),
    CONSTRAINT fk_chbh_bai_hoc FOREIGN KEY (ma_bai_hoc) REFERENCES bai_hoc(ma_bai_hoc),
    UNIQUE (ma_lop_hoc, ma_bai_hoc)
);

-- Bảng Bài Học Bổ Sung
CREATE TABLE bai_hoc_bo_sung (
    ma_bai_hoc_bo_sung BIGSERIAL PRIMARY KEY,
    ma_lop_hoc BIGINT NOT NULL,
    tieu_de VARCHAR(255) NOT NULL,
    mo_ta TEXT,
    loai VARCHAR(20) NOT NULL,
    vi_tri FLOAT NOT NULL,
    an_hien BOOLEAN DEFAULT TRUE,
    duong_dan TEXT NOT NULL,
    loai_tep VARCHAR(10),
    du_lieu_dong JSONB,
    ngay_tao TIMESTAMP DEFAULT NOW(),
    ngay_cap_nhat TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_bhbs_lop_hoc FOREIGN KEY (ma_lop_hoc) REFERENCES lop_hoc(ma_lop_hoc)
);

-- Bảng Bài Kiểm Tra Bổ Sung
CREATE TABLE bai_kiem_tra_bo_sung (
    ma_bai_kiem_tra_bo_sung BIGSERIAL PRIMARY KEY,
    ma_bai_bo_sung BIGINT UNIQUE NOT NULL,
    diem_toi_thieu INT NOT NULL DEFAULT 70,
    thoi_gian INT,
    CONSTRAINT fk_bktbs_bhbs FOREIGN KEY (ma_bai_bo_sung) REFERENCES bai_hoc_bo_sung(ma_bai_hoc_bo_sung)
);

-- Bảng Câu Hỏi Bài Bổ Sung
CREATE TABLE cau_hoi_bai_bo_sung (
    ma_cau_hoi_bai_bo_sung BIGSERIAL PRIMARY KEY,
    ma_bai_kiem_tra_bo_sung BIGINT NOT NULL,
    noi_dung TEXT NOT NULL,
    vi_tri INT NOT NULL,
    loai_cau_hoi VARCHAR(20) NOT NULL DEFAULT 'MOT_DAP_AN',
    dap_an JSONB NOT NULL,
    CONSTRAINT fk_chbbs_bktbs FOREIGN KEY (ma_bai_kiem_tra_bo_sung) REFERENCES bai_kiem_tra_bo_sung(ma_bai_kiem_tra_bo_sung)
);

-- Bảng Tiến Độ Học
CREATE TABLE tien_do_hoc (
    ma_tien_do BIGSERIAL PRIMARY KEY,
    ma_hoc_vien BIGINT NOT NULL,
    ma_bai_hoc BIGINT,
    hoan_thanh BOOLEAN DEFAULT FALSE,
    vi_tri_dung BIGINT,
    ngay_hoan_thanh TIMESTAMP,
    CONSTRAINT fk_tdh_hoc_vien FOREIGN KEY (ma_hoc_vien) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT fk_tdh_bai_hoc FOREIGN KEY (ma_bai_hoc) REFERENCES bai_hoc(ma_bai_hoc),
    UNIQUE (ma_hoc_vien, ma_bai_hoc)
);

-- Bảng Tiến Độ Bài Bổ Sung
CREATE TABLE tien_do_bai_bo_sung (
    ma_tien_do_bo_sung BIGSERIAL PRIMARY KEY,
    ma_hoc_vien BIGINT NOT NULL,
    ma_bai_hoc_bo_sung BIGINT,
    hoan_thanh BOOLEAN DEFAULT FALSE,
    vi_tri_dung BIGINT,
    ngay_hoan_thanh TIMESTAMP,
    CONSTRAINT fk_tdbbs_hoc_vien FOREIGN KEY (ma_hoc_vien) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT fk_tdbbs_bhbs FOREIGN KEY (ma_bai_hoc_bo_sung) REFERENCES bai_hoc_bo_sung(ma_bai_hoc_bo_sung),
    UNIQUE (ma_hoc_vien, ma_bai_hoc_bo_sung)
);

-- Bảng Kết Quả Làm Bài
CREATE TABLE ket_qua_lam_bai (
    ma_ket_qua BIGSERIAL PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL,
    ma_lop BIGINT NOT NULL,
    ma_bai_kiem_tra BIGINT NOT NULL,
    diem INT NOT NULL,
    ket_qua BOOLEAN NOT NULL,
    thoi_diem_lam TIMESTAMP DEFAULT NOW(),
    chi_tiet_dap_an JSONB NOT NULL,
    CONSTRAINT fk_kqlb_nguoi_dung FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT fk_kqlb_lop_hoc FOREIGN KEY (ma_lop) REFERENCES lop_hoc(ma_lop_hoc),
    CONSTRAINT fk_kqlb_bkt FOREIGN KEY (ma_bai_kiem_tra) REFERENCES bai_kiem_tra(ma_bai_kiem_tra)
);

-- Bảng Kết Quả Làm Bài Bổ Sung
CREATE TABLE ket_qua_lam_bai_bo_sung (
    ma_ket_qua_bo_sung BIGSERIAL PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL,
    ma_lop BIGINT NOT NULL,
    ma_bai_kiem_tra_bo_sung BIGINT NOT NULL,
    diem INT NOT NULL,
    ket_qua BOOLEAN NOT NULL,
    thoi_diem_lam TIMESTAMP DEFAULT NOW(),
    chi_tiet_dap_an JSONB NOT NULL,
    CONSTRAINT fk_kqlbbs_nguoi_dung FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT fk_kqlbbs_lop_hoc FOREIGN KEY (ma_lop) REFERENCES lop_hoc(ma_lop_hoc),
    CONSTRAINT fk_kqlbbs_bktbs FOREIGN KEY (ma_bai_kiem_tra_bo_sung) REFERENCES bai_kiem_tra_bo_sung(ma_bai_kiem_tra_bo_sung)
);

-- Tạo Index
CREATE INDEX idx_tdh_hv_bh ON tien_do_hoc(ma_hoc_vien, ma_bai_hoc);
CREATE INDEX idx_tdh_hv_ht ON tien_do_hoc(ma_hoc_vien, hoan_thanh);
CREATE INDEX idx_tdbs_hv_bhbs ON tien_do_bai_bo_sung(ma_hoc_vien, ma_bai_hoc_bo_sung);
CREATE INDEX idx_kqlb_nd_bkt ON ket_qua_lam_bai(ma_nguoi_dung, ma_bai_kiem_tra);
