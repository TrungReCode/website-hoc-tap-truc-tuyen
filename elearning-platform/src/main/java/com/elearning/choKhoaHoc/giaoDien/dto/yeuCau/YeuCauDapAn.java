package com.elearning.choKhoaHoc.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record YeuCauDapAn(
        @NotNull Integer id,
        @NotBlank(message = "Nội dung phương án không được để trống") String noiDung,
        @NotNull Boolean dapAnDung
) {
}
