package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PhanHoiLop(
        Long maLopHoc,
        Long maKhoaHoc,
        String tenKhoaHoc,
        String anhBiaKhoaHoc,
        Long maGiangVien,
        String tenGiangVien,
        String maLop,
        LocalDateTime ngayBatDau,
        LocalDateTime ngayKetThuc,
        Integer siSo,
        Long soThanhVien,
        TrangThaiLop trangThai,
        BigDecimal phiLopHoc,
        Boolean congKhai,
        Integer tiLeHoanThanh,
        Integer tienDoPhanTramHocVien,
        Double diemTrungBinh,
        Long soLuongDanhGia
) {
}
