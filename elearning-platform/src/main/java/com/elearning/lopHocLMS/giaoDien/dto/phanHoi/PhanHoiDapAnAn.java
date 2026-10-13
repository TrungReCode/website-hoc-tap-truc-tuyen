package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

public record PhanHoiDapAnAn(
        Integer id,
        String noiDung
        // KHÔNG có trường dapAnDung — chống gian lận khi đang làm bài
) {
}
