package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** QTV_CAO_CAP điều chỉnh thông số toàn cục (UC-13). */
public record YeuCauCapNhatCauHinhHeThong(

        @NotNull(message = "Vui lòng nhập tỷ lệ phí dịch vụ cho Giấy phép cá nhân")
        @DecimalMin(value = "0", message = "Tỷ lệ phí không được âm")
        @DecimalMax(value = "100", message = "Tỷ lệ phí không được vượt quá 100%")
        BigDecimal tyLePhiCaNhan,

        @NotNull(message = "Vui lòng nhập tỷ lệ phí dịch vụ cho Giấy phép giảng dạy")
        @DecimalMin(value = "0", message = "Tỷ lệ phí không được âm")
        @DecimalMax(value = "100", message = "Tỷ lệ phí không được vượt quá 100%")
        BigDecimal tyLePhiDayHoc,

        @NotNull(message = "Vui lòng nhập số ngày giữ ký quỹ khóa học")
        @Min(value = 0, message = "Số ngày không được âm")
        @Max(value = 365, message = "Số ngày tối đa 365")
        Integer soNgayGiuKyQuyKhoaHoc,

        @NotNull(message = "Vui lòng nhập số ngày giữ ký quỹ lớp học")
        @Min(value = 0, message = "Số ngày không được âm")
        @Max(value = 365, message = "Số ngày tối đa 365")
        Integer soNgayGiuKyQuyLopHoc
) {
}