package com.elearning.lopHocLMS.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;

/**
 * viTriSau/viTriTruoc: giống cơ chế tạo mới (fractional indexing) — dùng để
 * di chuyển bài bổ sung tới vị trí mới trong giáo trình lai. Cả 2 = null
 * nghĩa là giữ nguyên vị trí hiện tại (chỉ đổi tiêu đề/mô tả/ẩn-hiện).
 */
public record YeuCauCapNhatBaiBoSung(
        @NotBlank(message = "Tiêu đề không được để trống") String tieuDe,
        String moTa,
        boolean anHien,
        Float viTriSau,
        Float viTriTruoc
) {
}