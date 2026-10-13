package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi;

public record TaiKhoanNganHangDto(
        Long maTaiKhoan,
        Long maGiangVien,
        String tenChuTaiKhoan,
        String soTaiKhoan,
        String tenNganHang,
        Boolean macDinh
) {
}
