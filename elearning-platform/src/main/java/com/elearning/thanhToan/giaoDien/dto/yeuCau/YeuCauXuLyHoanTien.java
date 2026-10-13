package com.elearning.thanhToan.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Quyết định của quản trị viên tài chính đối với một yêu cầu hoàn tiền.
 * trangThai chỉ chấp nhận HOAN_TAT (đồng ý) hoặc TU_CHOI (từ chối).
 */
public record YeuCauXuLyHoanTien(
        @NotBlank
        @Pattern(regexp = "HOAN_TAT|TU_CHOI", message = "Trạng thái chỉ có thể là HOAN_TAT hoặc TU_CHOI")
        String trangThai,
        String ghiChu
) {
}