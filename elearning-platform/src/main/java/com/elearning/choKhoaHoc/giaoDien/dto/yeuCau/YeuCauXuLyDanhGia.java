package com.elearning.choKhoaHoc.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;

public record YeuCauXuLyDanhGia(
        @NotBlank(message = "Vui lòng nhập lý do xử lý để học viên được biết") String lyDo
) {
}
