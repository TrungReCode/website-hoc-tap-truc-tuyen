package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

import java.util.List;

public record PhanHoiCauHoiTrongLop(
        Long maCauHoi,
        String noiDung,
        Integer viTri,
        String loaiCauHoi,
        List<PhanHoiDapAnAn> dapAn,
        List<String> luaChonGhep // vế phải của câu GHEP_CAP đã xáo trộn; rỗng với dạng khác
) {
}
