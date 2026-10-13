package com.elearning.choKhoaHoc.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record YeuCauCapNhatKhoaHoc(
        @NotBlank(message = "Tiêu đề khóa học không được để trống") String tieuDe,
        String mieuTa,
        @NotNull(message = "Vui lòng chọn danh mục") Long maDanhMuc,
        @NotNull @DecimalMin(value = "0", message = "Giá không được âm") BigDecimal gia,
        @DecimalMin(value = "0", message = "Giá giấy phép giảng dạy không được âm") BigDecimal phiDayHoc,
        String anhBia
) {
}
