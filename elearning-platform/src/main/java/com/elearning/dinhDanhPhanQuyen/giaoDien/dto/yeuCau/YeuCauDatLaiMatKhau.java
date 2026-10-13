package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record YeuCauDatLaiMatKhau(
        @NotBlank String token,
        @NotBlank @Size(min = 8, message = "Mật khẩu mới phải có ít nhất 8 ký tự") String matKhauMoi,
        @NotBlank String xacNhanMatKhauMoi
) {
}