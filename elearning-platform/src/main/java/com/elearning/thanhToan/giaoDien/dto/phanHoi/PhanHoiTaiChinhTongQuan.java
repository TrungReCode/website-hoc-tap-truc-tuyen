package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import java.math.BigDecimal;

public record PhanHoiTaiChinhTongQuan(
        BigDecimal tongDoanhThuKhoaHoc,
        BigDecimal tongDoanhThuLopHoc,
        BigDecimal tongDoanhThu,
        long soGiaoDichThanhCong,
        long soGiaoDichChoXacNhan,
        long soPhieuChoXacNhan,
        long soPhieuDangXuLy,
        long soPhieuHoanTat,
        long soPhieuThatBai,
        long soYeuCauHoanTienChoXuLy
) {}