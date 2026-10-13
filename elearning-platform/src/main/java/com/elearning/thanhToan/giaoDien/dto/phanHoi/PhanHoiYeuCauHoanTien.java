package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import com.elearning.thanhToan.mienDomain.TrangThaiHoanTien;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PhanHoiYeuCauHoanTien(
        Long maHoanTien,
        Long maPhieuGiaoDich,
        BigDecimal soTien,
        TrangThaiHoanTien trangThai,
        LocalDateTime ngayTao
) {
}