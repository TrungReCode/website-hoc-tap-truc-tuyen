package com.elearning.lopHocLMS.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record YeuCauCauTraLoi(
        @NotNull Long maCauHoi,
        List<Integer> dsMaDapAnDaChon
) {
}
