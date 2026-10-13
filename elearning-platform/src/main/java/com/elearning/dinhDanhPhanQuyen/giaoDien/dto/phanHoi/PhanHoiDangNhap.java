package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi;

public record PhanHoiDangNhap(
        String accessToken,
        String refreshToken,
        String hoTen,
        String email,
        String vaiTro,
        Long maNguoiDung
) {
}
