-- Thêm cột du_lieu_dong kiểu JSONB cho bảng phiếu mua khóa học
ALTER TABLE phieu_mua_khoa_hoc 
ADD COLUMN du_lieu_dong JSONB DEFAULT '{}';

-- Thêm cột du_lieu_dong kiểu JSONB cho bảng phiếu trả phí lớp học
ALTER TABLE phieu_tra_phi_lop_hoc 
ADD COLUMN du_lieu_dong JSONB DEFAULT '{}';