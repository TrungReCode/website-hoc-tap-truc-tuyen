package com.elearning.choKhoaHoc.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * maDanhMucCha = null -> tạo danh mục gốc (mức 1).
 * maDanhMucCha != null -> tạo danh mục con (mức 2) — chỉ hỗ trợ phân cấp 2 mức.
 */
public record YeuCauTaoDanhMuc(
        @NotBlank(message = "Tên danh mục không được để trống")
        @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
        String tenDanhMuc,
        Long maDanhMucCha
) {
}
