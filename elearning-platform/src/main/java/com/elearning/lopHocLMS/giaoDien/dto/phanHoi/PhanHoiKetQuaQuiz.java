package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

import java.time.LocalDateTime;
import java.util.List;

public record PhanHoiKetQuaQuiz(
        Long maKetQua,
        Integer diem,
        boolean datYeuCau,
        Integer diemToiThieu,
        LocalDateTime thoiDiemLam,
        List<PhanHoiCauHoiDaCham> chiTietCauHoi
) {
}
