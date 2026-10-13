package com.elearning.choKhoaHoc.giaoDien.dto.phanHoi;

import java.util.List;

public record PhanHoiCauHoi(
        Long maCauHoi,
        String noiDung,
        Integer viTri,
        String loaiCauHoi,
        List<PhanHoiDapAn> dapAn
) {
}
