package com.elearning.choKhoaHoc.giaoDien.dto.phanHoi;

import java.util.List;

public record PhanHoiChuong(
        Long maChuong,
        String tieuDe,
        Integer viTri,
        List<PhanHoiBaiHoc> danhSachBaiHoc
) {
}
