package com.elearning.thanhToan.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;

public record YeuCauHoanTien(
        @NotBlank String lyDo
) {
}