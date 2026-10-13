package com.elearning.lopHocLMS.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record YeuCauMoiHocVien(
        @NotEmpty(message = "Vui lòng nhập ít nhất 1 email") List<@Email(message = "Email không hợp lệ") String> dsEmail
) {
}
