package com.elearning.thanhToan.giaoDien.dto.phanHoi;

public record PhanHoiXuLyVnpay(
        String rspCode,
        String message,
        String maGiaoDichVnpay,
        boolean thanhCong
) {
}