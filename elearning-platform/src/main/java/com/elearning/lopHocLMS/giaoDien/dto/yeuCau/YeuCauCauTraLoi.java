package com.elearning.lopHocLMS.giaoDien.dto.yeuCau;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Bài làm của 1 câu — chỉ trường tương ứng với dạng câu hỏi mới được dùng:
 *   MOT_DAP_AN / NHIEU_DAP_AN / DUNG_SAI : dsMaDapAnDaChon
 *   SAP_XEP      : dsMaDapAnDaChon theo THỨ TỰ học viên đã sắp
 *   TRA_LOI_NGAN : traLoiVanBan
 *   GHEP_CAP     : capGhep
 */
public record YeuCauCauTraLoi(
        @NotNull Long maCauHoi,
        List<Integer> dsMaDapAnDaChon,
        @Size(max = 1000) String traLoiVanBan,
        @Size(max = 100) @Valid List<CapGhepTraLoi> capGhep
) {
}
