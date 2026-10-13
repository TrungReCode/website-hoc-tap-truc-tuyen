package com.elearning.lopHocLMS.giaoDien.dto.yeuCau;

import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiHocLieuBoSung;

/**
 * viTriSau/viTriTruoc: vị trí (đã merge cùng bài gốc) của 2 mục liền kề nơi
 * giảng viên muốn chèn bài mới vào giữa — Service tự tính vị trí FLOAT mới
 * bằng trung bình cộng (fractional indexing), null ở 1 hoặc cả 2 đầu nghĩa
 * là chèn vào đầu/cuối danh sách.
 */
public record YeuCauTaoBaiBoSung(
        String tieuDe,
        String moTa,
        LoaiHocLieuBoSung loai,
        boolean anHien,
        Float viTriSau,
        Float viTriTruoc,
        String duongDanLink, // chỉ dùng khi loai = LINK
        Integer diemToiThieu, // chỉ dùng khi loai = QUIZ
        Integer thoiGian      // chỉ dùng khi loai = QUIZ
) {
}
