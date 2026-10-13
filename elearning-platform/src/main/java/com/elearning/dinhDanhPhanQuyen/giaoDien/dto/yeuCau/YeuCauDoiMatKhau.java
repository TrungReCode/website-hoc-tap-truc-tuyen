package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record YeuCauDoiMatKhau(
        @NotBlank String matKhauHienTai,
        @NotBlank @Size(min = 8, max = 128) String matKhauMoi,
        @NotBlank String xacNhanMatKhauMoi
) {
}
