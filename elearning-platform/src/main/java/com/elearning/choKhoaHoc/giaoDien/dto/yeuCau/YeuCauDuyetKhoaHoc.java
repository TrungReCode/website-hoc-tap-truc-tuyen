package com.elearning.choKhoaHoc.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotNull;

public record YeuCauDuyetKhoaHoc(
        @NotNull(message = "Vui lòng chọn hành động") HanhDongDuyet hanhDong,
        String ghiChu // bắt buộc có nội dung khi hanhDong = TU_CHOI, Service tự kiểm tra
) {
}
