package com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau;

import com.elearning.traoDoiTrongLop.mienDomain.TepDinhKem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record YeuCauCapNhatBaiDang(

        @NotBlank(message = "Tiêu đề không được để trống")
        @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
        String tieuDe,

        String noiDung,

        List<TepDinhKem> tepDinhKem
) {}
