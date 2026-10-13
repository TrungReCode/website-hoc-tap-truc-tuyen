package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record YeuCauQuenMatKhau(
        @NotBlank @Email String email
) {
}