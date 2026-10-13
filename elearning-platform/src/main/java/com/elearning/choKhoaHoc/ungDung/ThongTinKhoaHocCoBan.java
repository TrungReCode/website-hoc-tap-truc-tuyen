package com.elearning.choKhoaHoc.ungDung;

import com.elearning.choKhoaHoc.mienDomain.enumDomain.TrangThaiKhoaHoc;

import java.math.BigDecimal;

/**
 * Thông tin tối thiểu mà module lopHocLMS cần khi tạo lớp học dựa trên một
 * khóa học (kiểm tra trạng thái CONG_KHAI, lấy giá Giấy phép Giảng dạy để
 * đối chiếu Double-Lock Validation, hiển thị tiêu đề khóa học gốc).
 */
public record ThongTinKhoaHocCoBan(
        Long maKhoaHoc,
        Long maTacGia,
        String tieuDe,
        String anhBia,
        BigDecimal gia,
        BigDecimal phiDayHoc,
        TrangThaiKhoaHoc trangThai
) {
}
