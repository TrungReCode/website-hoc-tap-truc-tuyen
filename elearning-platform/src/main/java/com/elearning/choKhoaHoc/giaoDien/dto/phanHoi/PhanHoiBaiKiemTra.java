package com.elearning.choKhoaHoc.giaoDien.dto.phanHoi;

import java.util.List;

public record PhanHoiBaiKiemTra(
        Long maBaiKiemTra,
        Integer diemToiThieu,
        Integer thoiGian,
        List<PhanHoiCauHoi> danhSachCauHoi
) {
}
