package com.elearning.choKhoaHoc.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record YeuCauTaoChuong(
        @NotBlank(message = "Tiêu đề chương không được để trống") String tieuDe,
        @NotNull(message = "Vị trí chương không được để trống") Integer viTri
) {
}
