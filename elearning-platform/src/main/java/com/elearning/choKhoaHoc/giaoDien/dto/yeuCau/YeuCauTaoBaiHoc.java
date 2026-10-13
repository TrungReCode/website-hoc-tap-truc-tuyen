package com.elearning.choKhoaHoc.giaoDien.dto.yeuCau;

import com.elearning.choKhoaHoc.mienDomain.enumDomain.LoaiBaiHoc;

/**
 * Không gắn @Valid/Bean Validation trực tiếp vì endpoint tạo bài học là
 * multipart/form-data (kèm file) — Controller nhận từng @RequestParam rồi
 * tự dựng record này để truyền xuống Service, validate thủ công ở Service.
 *
 * diemToiThieu/thoiGian chỉ có ý nghĩa khi loai = QUIZ (tạo kèm BaiKiemTra rỗng).
 */
public record YeuCauTaoBaiHoc(
        String tieuDe,
        LoaiBaiHoc loai,
        Integer viTri,
        boolean xemThu,
        Integer diemToiThieu,
        Integer thoiGian
) {
}
