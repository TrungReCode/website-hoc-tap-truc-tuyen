package com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;

public record YeuCauTaoBinhLuan(

        @NotBlank(message = "Nội dung bình luận không được để trống")
        String noiDung,

        /** NULL = bình luận gốc, != NULL = reply của bình luận có mã này. */
        Long maCha
) {}
