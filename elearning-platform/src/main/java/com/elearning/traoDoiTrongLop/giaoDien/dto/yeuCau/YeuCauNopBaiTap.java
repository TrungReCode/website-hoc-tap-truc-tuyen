package com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau;

import com.elearning.traoDoiTrongLop.mienDomain.TepDinhKem;

import java.util.List;

/** Học viên nộp bài tập (UC kiểu Google Classroom) — bài đăng phải là loại BAI_TAP. */
public record YeuCauNopBaiTap(

        String ghiChu,

        List<TepDinhKem> tepDinhKem
) {}
