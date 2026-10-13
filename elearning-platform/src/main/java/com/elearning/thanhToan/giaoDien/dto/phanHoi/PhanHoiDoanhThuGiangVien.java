package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import java.math.BigDecimal;

/**
 * Tổng quan doanh thu dành cho GIẢNG VIÊN xem về chính mình
 * (khác PhanHoiTaiChinhTongQuan — dành cho admin, toàn hệ thống).
 */
public record PhanHoiDoanhThuGiangVien(
        int soKhoaHoc,
        int soLopHoc,
        BigDecimal tongDoanhThuKhoaHoc,
        BigDecimal tongDoanhThuLopHoc,
        BigDecimal tongDoanhThu,       // gộp, trước khi trừ hoa hồng
        BigDecimal tongThucNhan,       // tổng số tiền đã thực nhận (các phiếu đối soát HOAN_TAT)
        long soPhieuChoXacNhan,
        long soPhieuDangXuLy,
        long soPhieuHoanTat,
        long soPhieuThatBai,
        boolean coTaiKhoanNganHang
) {
}