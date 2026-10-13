package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Một dòng giao dịch doanh thu (mua khóa học hoặc đóng phí lớp học) — dùng cho bảng lọc, biểu đồ và xuất CSV. */
public record PhanHoiGiaoDichDoanhThu(
        Long maPhieu,
        String loai,                 // "KHOA_HOC" | "LOP_HOC"
        LocalDateTime ngayGiaoDich,
        Long maKhoaHoc,
        String tenKhoaHoc,
        String maLop,                 // null nếu là giao dịch mua khóa học (không qua lớp)
        Long maGiangVien,
        String tenGiangVien,
        BigDecimal soTien,
        String trangThai,
        /** true nếu giao dịch đã thuộc về 1 phiếu đối soát ở trạng thái HOAN_TAT (tiền đã thực sự về tài khoản). */
        boolean daGiaiNgan
) {
}
