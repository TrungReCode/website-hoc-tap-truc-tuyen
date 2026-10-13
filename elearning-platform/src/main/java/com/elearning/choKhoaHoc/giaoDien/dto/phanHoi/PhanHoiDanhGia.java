package com.elearning.choKhoaHoc.giaoDien.dto.phanHoi;

import java.time.LocalDateTime;

public record PhanHoiDanhGia(
        Long maDanhGia,
        Long maNguoiDung,
        String tenNguoiDung,
        String anhDaiDienNguoiDung,
        Integer soSao,
        String nhanXet,
        LocalDateTime ngayTao,
        Boolean an
) {
}
