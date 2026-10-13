package com.elearning.lopHocLMS.giaoDien.dto.yeuCau;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record YeuCauNopBaiKiemTra(
        @NotEmpty(message = "Cần trả lời ít nhất 1 câu hỏi") @Valid List<YeuCauCauTraLoi> cauTraLoi
) {
}
