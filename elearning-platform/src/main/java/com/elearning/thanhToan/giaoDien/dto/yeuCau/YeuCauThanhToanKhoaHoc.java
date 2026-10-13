package com.elearning.thanhToan.giaoDien.dto.yeuCau;

import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiGiayPhep;
import jakarta.validation.constraints.NotNull;

public record YeuCauThanhToanKhoaHoc(
        @NotNull LoaiGiayPhep loaiGiayPhep
) {
}