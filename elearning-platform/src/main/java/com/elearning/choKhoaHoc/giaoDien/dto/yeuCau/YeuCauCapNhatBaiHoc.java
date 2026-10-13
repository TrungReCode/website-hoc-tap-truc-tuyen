package com.elearning.choKhoaHoc.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record YeuCauCapNhatBaiHoc(
        @NotBlank(message = "Tiêu đề bài học không được để trống") String tieuDe,
        @NotNull(message = "Vị trí bài học không được để trống") Integer viTri,
        boolean xemThu
) {
}
