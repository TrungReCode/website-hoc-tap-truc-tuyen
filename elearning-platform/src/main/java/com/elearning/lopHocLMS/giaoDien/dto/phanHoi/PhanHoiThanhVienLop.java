package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

import java.time.LocalDateTime;

public record PhanHoiThanhVienLop(
        Long maHocVien,
        String tenHocVien,
        String anhDaiDienHocVien,
        /** Chỉ có giá trị khi giảng viên xem danh sách học viên của lớp mình (để liên hệ). */
        String emailHocVien,
        LocalDateTime ngayThamGia,
        Integer tienDoPhanTram
) {
}
