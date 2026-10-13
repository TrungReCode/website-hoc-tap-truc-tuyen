package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

import java.time.LocalDateTime;

public record PhanHoiDanhGiaLop(
        Long maDanhGia,
        Long maHocVien,
        String tenHocVien,
        String anhDaiDienHocVien,
        Integer soSao,
        String nhanXet,
        LocalDateTime ngayTao
) {
}
