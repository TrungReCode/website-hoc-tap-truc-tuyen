package com.elearning.lopHocLMS.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record YeuCauDapAnBoSung(
        @NotNull Integer id,
        @NotBlank(message = "Nội dung phương án không được để trống") String noiDung,
        @NotNull Boolean dapAnDung,
        /** Chỉ dùng cho câu GHEP_CAP: vế phải ứng với noiDung (vế trái). */
        @Size(max = 1000) String noiDungGhep
) {
}
