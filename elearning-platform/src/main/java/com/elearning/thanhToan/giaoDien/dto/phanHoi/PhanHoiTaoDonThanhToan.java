package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import com.elearning.thanhToan.mienDomain.TrangThaiGiaoDich;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PhanHoiTaoDonThanhToan(
        String maGiaoDichVnpay,
        BigDecimal soTien,
        String urlThanhToan,
        TrangThaiGiaoDich trangThai,
        LocalDateTime ngayHetHan
) {
}