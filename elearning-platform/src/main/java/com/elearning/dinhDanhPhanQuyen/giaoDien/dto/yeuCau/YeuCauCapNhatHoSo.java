package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.Size;

public record YeuCauCapNhatHoSo(
        @Size(min = 1, max = 100)
        String hoTen,
        @Size(max = 15)
        String sdt,
        String anhDaiDien,
        @Size(max = 1000)
        String tieuSu
) {
}
