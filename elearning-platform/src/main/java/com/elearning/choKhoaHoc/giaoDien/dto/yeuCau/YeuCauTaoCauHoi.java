package com.elearning.choKhoaHoc.giaoDien.dto.yeuCau;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record YeuCauTaoCauHoi(
        @NotBlank(message = "Nội dung câu hỏi không được để trống") String noiDung,
        @NotNull(message = "Vị trí câu hỏi không được để trống") Integer viTri,
        String loaiCauHoi, // MOT_DAP_AN | NHIEU_DAP_AN | DUNG_SAI — chuỗi để Service tự validate/parse, tránh ràng buộc enum package-private vào DTO public
        @NotEmpty(message = "Câu hỏi cần ít nhất 2 phương án trả lời") @Valid List<YeuCauDapAn> dapAn
) {
}
