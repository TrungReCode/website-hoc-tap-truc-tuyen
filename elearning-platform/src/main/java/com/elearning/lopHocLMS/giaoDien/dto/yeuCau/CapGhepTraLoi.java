package com.elearning.lopHocLMS.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.Size;

/** Một cặp học viên đã nối ở câu GHEP_CAP: mục vế trái {@code maDapAn} nối với vế phải {@code noiDungGhep}. */
public record CapGhepTraLoi(
        Integer maDapAn,
        @Size(max = 1000) String noiDungGhep
) {
}
