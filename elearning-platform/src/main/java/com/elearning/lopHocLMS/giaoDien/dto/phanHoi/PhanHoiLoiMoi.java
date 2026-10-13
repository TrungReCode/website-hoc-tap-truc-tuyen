package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLoiMoi;

import java.time.LocalDateTime;

public record PhanHoiLoiMoi(
        Long maLoiMoi,
        Long maLopHoc,
        String maLop,
        String tenKhoaHoc,
        String tenGiangVien,
        String emailHocVien,
        TrangThaiLoiMoi trangThai,
        LocalDateTime ngayTao,
        LocalDateTime ngayHetHan
) {
}
