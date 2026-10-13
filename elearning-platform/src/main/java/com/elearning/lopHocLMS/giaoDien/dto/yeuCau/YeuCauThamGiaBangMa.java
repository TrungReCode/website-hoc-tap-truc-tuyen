package com.elearning.lopHocLMS.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;

public record YeuCauThamGiaBangMa(
        @NotBlank(message = "Vui lòng nhập mã lớp") String maLop
) {
}
