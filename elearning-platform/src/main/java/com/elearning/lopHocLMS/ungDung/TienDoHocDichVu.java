package com.elearning.lopHocLMS.ungDung;

import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCapNhatTienDo;

public interface TienDoHocDichVu {

    void capNhatTienDoBaiGoc(Long maNguoiDung, Long maLopHoc, Long maBaiHoc, YeuCauCapNhatTienDo yeuCau);

    void capNhatTienDoBaiBoSung(Long maNguoiDung, Long maBaiHocBoSung, YeuCauCapNhatTienDo yeuCau);

    /** % bài học (gốc + bổ sung, không tính loại QUIZ) đã hoàn thành trên tổng số — dùng hiển thị và sau này cho điều kiện giải ngân của thanhToan. */
    int tinhTienDoPhanTram(Long maNguoiDung, Long maLopHoc);

    /**
     * Đánh dấu hoàn thành bài học GỐC khi TỰ HỌC (không qua lớp) — chỉ yêu
     * cầu đã sở hữu khóa học (bất kỳ loại giấy phép), không cần là thành
     * viên lớp nào. Ghi chung vào đúng bảng tien_do_hoc với luồng trong lớp
     * (bảng này vốn không phân biệt lớp), nên tiến độ tự học và tiến độ khi
     * học trong lớp CHIA SẺ lẫn nhau — học 1 lần, tính cả 2 nơi.
     */
    void capNhatTienDoCaNhan(Long maNguoiDung, Long maBaiHoc, YeuCauCapNhatTienDo yeuCau);
}
