package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record YeuCauTaiKhoanNganHang(
        @NotBlank @Size(max = 100) String tenChuTaiKhoan,
        @NotBlank @Size(max = 30) String soTaiKhoan,
        @NotBlank @Size(max = 100) String tenNganHang
) {
}
