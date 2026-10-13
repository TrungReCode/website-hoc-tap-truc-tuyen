package com.elearning.chungChi.giaoDien.dto.phanHoi;

import java.math.BigDecimal;

public record PhanHoiTienDo(
        Long maTienDo,
        /** maKhoaHoc hoặc maLopHoc tùy loại */
        Long maTaiNguyen,
        String loai,             // "CA_NHAN" | "LOP_HOC"
        int tongBaiHoc,
        int baiHoanThanh,
        int tongQuiz,
        int quizDat,
        BigDecimal diemTrungBinh,
        BigDecimal phanTramHoanThanh,
        boolean daDuDieuKien
) {}
