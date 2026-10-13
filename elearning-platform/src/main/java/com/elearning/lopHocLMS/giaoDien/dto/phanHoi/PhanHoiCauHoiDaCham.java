package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

import java.util.List;

public record PhanHoiCauHoiDaCham(
        Long maCauHoi,
        String noiDung,
        List<PhanHoiDapAn> dapAn, // có dapAnDung — chỉ trả sau khi đã nộp bài
        List<Integer> dsMaDapAnDaChon,
        boolean dungCauNay
) {
}
