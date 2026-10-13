package com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Giảng viên chấm điểm một bài nộp. */
public record YeuCauChamDiem(

        @NotNull(message = "Vui lòng nhập điểm")
        @DecimalMin(value = "0", message = "Điểm không được âm")
        BigDecimal diem,

        @NotNull(message = "Vui lòng nhập điểm tối đa")
        @DecimalMin(value = "0.01", message = "Điểm tối đa phải lớn hơn 0")
        BigDecimal diemToiDa,

        String nhanXet
) {}
