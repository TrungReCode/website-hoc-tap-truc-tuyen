package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import com.elearning.thanhToan.mienDomain.TrangThaiGiaoDich;
import com.elearning.thanhToan.mienDomain.TrangThaiHoanTien;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Một dòng trong lịch sử giao dịch của người dùng — gộp cả giao dịch
 * mua khóa học (KHOA_HOC) và đóng phí lớp học (LOP_HOC).
 */
public record PhanHoiGiaoDich(
        String loaiGiaoDich,       // "KHOA_HOC" | "LOP_HOC"
        Long maPhieu,
        String tieuDe,
        Long maDoiTuong,           // maKhoaHoc hoặc maLopHoc
        BigDecimal soTien,
        String maGiaoDichVnpay,
        TrangThaiGiaoDich trangThai,
        TrangThaiHoanTien trangThaiHoanTien, // null nếu chưa từng yêu cầu hoàn tiền
        LocalDateTime hanGiuTien,
        LocalDateTime ngayTao,
        LocalDateTime ngayMuaThanhCong,
        boolean coTheYeuCauHoanTien
) {
}