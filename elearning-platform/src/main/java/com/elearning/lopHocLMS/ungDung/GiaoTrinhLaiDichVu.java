package com.elearning.lopHocLMS.ungDung;

import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiMucGiaoTrinhLai;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCauHinhBaiGoc;

import java.util.List;

public interface GiaoTrinhLaiDichVu {

    /** Cho Giảng viên quản lý — trả về cả mục đang ẩn. */
    List<PhanHoiMucGiaoTrinhLai> layGiaoTrinhLaiChoGiangVien(Long maNguoiDungGiangVien, Long maLopHoc);

    /**
     * Cho Quản trị viên nội dung giám sát — trả về cả mục đang ẩn, KHÔNG yêu
     * cầu là giảng viên phụ trách lớp. Cần thiết vì bài học bổ sung do
     * giảng viên thêm trực tiếp vào lớp KHÔNG đi qua luồng kiểm duyệt khóa
     * học (DuyetKhoaHocDichVu) — đây là kênh duy nhất để admin kiểm tra nội
     * dung này khi có báo cáo vi phạm.
     */
    List<PhanHoiMucGiaoTrinhLai> layGiaoTrinhLaiChoAdmin(Long maLopHoc);

    /** Cho Học viên học — chỉ trả mục đang hiện, kèm tiến độ cá nhân, yêu cầu đã là thành viên lớp. */
    List<PhanHoiMucGiaoTrinhLai> layGiaoTrinhLaiChoHocVien(Long maNguoiDung, Long maLopHoc);

    void capNhatCauHinhBaiGoc(Long maNguoiDungGiangVien, Long maLopHoc, Long maBaiHoc, YeuCauCauHinhBaiGoc yeuCau);
}
