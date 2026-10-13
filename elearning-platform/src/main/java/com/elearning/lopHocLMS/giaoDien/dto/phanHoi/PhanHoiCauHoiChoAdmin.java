package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

import java.util.List;

public record PhanHoiCauHoiChoAdmin(
        Long maCauHoi,
        String noiDung,
        Integer viTri,
        String loaiCauHoi,
        List<PhanHoiDapAnChoAdmin> dapAn
) {
}
