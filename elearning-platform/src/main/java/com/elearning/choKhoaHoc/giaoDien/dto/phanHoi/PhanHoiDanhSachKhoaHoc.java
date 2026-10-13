package com.elearning.choKhoaHoc.giaoDien.dto.phanHoi;

import com.elearning.choKhoaHoc.mienDomain.enumDomain.TrangThaiKhoaHoc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PhanHoiDanhSachKhoaHoc(
        Long maKhoaHoc,
        String tieuDe,
        String anhBia,
        String tenDanhMuc,
        Long maTacGia,
        String tenTacGia,
        BigDecimal gia,
        BigDecimal phiDayHoc,
        TrangThaiKhoaHoc trangThai,
        Double diemTrungBinh,
        Long soLuongDanhGia,
        Long soBaiHoc,
        LocalDateTime ngayTao
) {
}
