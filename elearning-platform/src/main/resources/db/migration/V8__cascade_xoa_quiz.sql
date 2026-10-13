-- V8: Bổ sung ON DELETE CASCADE cho các khóa ngoại quiz/câu hỏi
--
-- Lý do: bai_kiem_tra/cau_hoi (choKhoaHoc) và bai_kiem_tra_bo_sung/
-- cau_hoi_bai_bo_sung (lopHocLMS) KHÔNG được map như quan hệ JPA hai
-- chiều có cascade từ phía BaiHoc/BaiHocBoSung (chỉ có FK một chiều từ
-- phía BaiKiemTra trỏ tới BaiHoc). Do đó khi xóa một bài học/bài bổ sung
-- loại QUIZ đã có sẵn bài kiểm tra + câu hỏi, JPA sẽ không tự cascade xóa
-- xuống được, gây lỗi vi phạm khóa ngoại ở tầng DB. Thêm ON DELETE CASCADE
-- trực tiếp ở DB là cách sửa gọn nhất, không phụ thuộc cách entity được
-- map ở tầng Java.
--
-- Không cần đụng tới chuong->khoa_hoc hay bai_hoc->chuong vì 2 quan hệ đó
-- đã được map đầy đủ hai chiều với cascade=ALL + orphanRemoval=true ở
-- tầng JPA (KhoaHoc.danhSachChuong, Chuong.danhSachBaiHoc), Hibernate tự
-- xóa đúng thứ tự mà không cần CASCADE ở DB.

ALTER TABLE bai_kiem_tra DROP CONSTRAINT fk_bkt_bai_hoc;
ALTER TABLE bai_kiem_tra ADD CONSTRAINT fk_bkt_bai_hoc
    FOREIGN KEY (ma_bai_hoc) REFERENCES bai_hoc(ma_bai_hoc) ON DELETE CASCADE;

ALTER TABLE cau_hoi DROP CONSTRAINT fk_ch_bkt;
ALTER TABLE cau_hoi ADD CONSTRAINT fk_ch_bkt
    FOREIGN KEY (ma_bai_kiem_tra) REFERENCES bai_kiem_tra(ma_bai_kiem_tra) ON DELETE CASCADE;

ALTER TABLE bai_kiem_tra_bo_sung DROP CONSTRAINT fk_bktbs_bhbs;
ALTER TABLE bai_kiem_tra_bo_sung ADD CONSTRAINT fk_bktbs_bhbs
    FOREIGN KEY (ma_bai_bo_sung) REFERENCES bai_hoc_bo_sung(ma_bai_hoc_bo_sung) ON DELETE CASCADE;

ALTER TABLE cau_hoi_bai_bo_sung DROP CONSTRAINT fk_chbbs_bktbs;
ALTER TABLE cau_hoi_bai_bo_sung ADD CONSTRAINT fk_chbbs_bktbs
    FOREIGN KEY (ma_bai_kiem_tra_bo_sung) REFERENCES bai_kiem_tra_bo_sung(ma_bai_kiem_tra_bo_sung) ON DELETE CASCADE;
