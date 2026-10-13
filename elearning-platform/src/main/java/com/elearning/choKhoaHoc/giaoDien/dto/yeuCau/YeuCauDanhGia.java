package com.elearning.choKhoaHoc.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record YeuCauDanhGia(
        @NotNull(message = "Vui lòng chọn số sao") @Min(1) @Max(5) Integer soSao,
        @Size(max = 1000, message = "Nhận xét tối đa 1000 ký tự") String nhanXet
) {
}
