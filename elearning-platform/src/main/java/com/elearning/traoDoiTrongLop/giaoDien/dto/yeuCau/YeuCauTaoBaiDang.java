package com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau;

import com.elearning.traoDoiTrongLop.mienDomain.TepDinhKem;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiBaiDang;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record YeuCauTaoBaiDang(

        @NotNull(message = "Loại bài đăng không được để trống")
        LoaiBaiDang loai,

        @NotBlank(message = "Tiêu đề không được để trống")
        @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
        String tieuDe,

        String noiDung,

        List<TepDinhKem> tepDinhKem
) {}
