package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import com.elearning.thanhToan.mienDomain.TrangThaiDoiSoat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PhanHoiPhieuDoiSoat(
        Long maDoiSoat,
        Long maTaiKhoan,
        Long maGiangVien,
        String tenGiangVien,
        String tenChuTaiKhoan,
        String soTaiKhoan,
        String tenNganHang,
        BigDecimal hoaHong,
        BigDecimal tongDoanhThu,
        BigDecimal soTienThucTe,
        BigDecimal chenhLech,
        TrangThaiDoiSoat trangThai,
        String maGiaoDichNganHang,
        LocalDateTime ngayTao,
        LocalDateTime ngayChuyenThanhCong
) {}