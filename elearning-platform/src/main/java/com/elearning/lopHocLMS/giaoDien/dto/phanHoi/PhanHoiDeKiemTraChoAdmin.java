package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

import java.util.List;

/** Dùng riêng cho Quản trị viên xem nội dung Quiz để kiểm duyệt — không cần giayConLai vì admin không "làm bài" thật. */
public record PhanHoiDeKiemTraChoAdmin(
        String loaiNguon, // GOC | BO_SUNG
        Long maBaiKiemTra,
        String tieuDe,
        Integer diemToiThieu,
        Integer thoiGian,
        List<PhanHoiCauHoiChoAdmin> danhSachCauHoi
) {
}
