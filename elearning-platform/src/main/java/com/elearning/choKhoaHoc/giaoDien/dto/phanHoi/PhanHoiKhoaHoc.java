package com.elearning.choKhoaHoc.giaoDien.dto.phanHoi;

import com.elearning.choKhoaHoc.mienDomain.enumDomain.TrangThaiKhoaHoc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PhanHoiKhoaHoc(
        Long maKhoaHoc,
        Long maTacGia,
        String tenTacGia,
        String anhDaiDienTacGia,
        String tieuDe,
        String mieuTa,
        String anhBia,
        Long maDanhMuc,
        String tenDanhMuc,
        BigDecimal gia,
        BigDecimal phiDayHoc,
        TrangThaiKhoaHoc trangThai,
        String lyDoTuChoi,
        LocalDateTime ngayTao,
        LocalDateTime ngayCapNhat,
        Double diemTrungBinh,
        Long soLuongDanhGia,
        List<PhanHoiChuong> danhSachChuong
) {
}
