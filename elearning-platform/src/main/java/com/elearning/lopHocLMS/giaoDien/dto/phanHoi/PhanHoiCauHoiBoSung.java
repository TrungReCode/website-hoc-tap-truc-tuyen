package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

import com.elearning.lopHocLMS.mienDomain.DapAnBoSung;

import java.util.List;

/**
 * Câu hỏi của bài kiểm tra bổ sung — dùng cho màn hình quản lý của giảng
 * viên, có tiết lộ đáp án đúng (khác PhanHoiCauHoi phía học viên làm bài).
 */
public record PhanHoiCauHoiBoSung(
        Long maCauHoiBaiBoSung,
        String noiDung,
        Integer viTri,
        String loaiCauHoi,
        List<DapAnBoSung> dapAn
) {
}