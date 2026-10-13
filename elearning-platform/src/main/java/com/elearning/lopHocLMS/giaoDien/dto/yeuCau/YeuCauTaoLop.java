package com.elearning.lopHocLMS.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record YeuCauTaoLop(
        @NotNull(message = "Vui lòng chọn khóa học gốc") Long maKhoaHoc,
        @NotNull(message = "Vui lòng chọn ngày khai giảng") LocalDateTime ngayBatDau,
        LocalDateTime ngayKetThuc,
        @Min(value = 1, message = "Sĩ số tối thiểu là 1") Integer siSo,
        @DecimalMin(value = "0", message = "Học phí không được âm") BigDecimal phiLopHoc,
        Boolean congKhai,
        Boolean muaKhoaHocGoc
) {
}
