package com.elearning.lopHocLMS.giaoDien.dto.yeuCau;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record YeuCauTaoCauHoiBoSung(
        @NotBlank(message = "Nội dung câu hỏi không được để trống") String noiDung,
        @NotNull Integer viTri,
        String loaiCauHoi,
        @NotEmpty(message = "Câu hỏi cần ít nhất 2 phương án trả lời") @Valid List<YeuCauDapAnBoSung> dapAn
) {
}
