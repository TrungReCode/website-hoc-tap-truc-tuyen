package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

import java.util.Map;

/**
 * Đại diện thống nhất cho 1 mục trong giáo trình lai, dù nguồn gốc là bài
 * học gốc (choKhoaHoc, đã áp dụng cau_hinh_bai_hoc của lớp) hay bài học
 * bổ sung (lopHocLMS tự tạo). loaiNguon cho frontend biết gọi API nào khi
 * cần thao tác sửa/xóa (GOC -> API choKhoaHoc, BO_SUNG -> API lopHocLMS).
 * Không lộ đáp án đúng của quiz — chỉ tiết lộ sau khi nộp bài.
 */
public record PhanHoiMucGiaoTrinhLai(
        String maMuc, // "GOC-{id}" hoặc "BS-{id}" - định danh duy nhất để render danh sách
        String loaiNguon, // GOC | BO_SUNG
        Long maThamChieu, // maBaiHoc hoặc maBaiHocBoSung tương ứng
        String tenChuongGoc, // chỉ có ở mục nguồn GOC, dùng để nhóm hiển thị
        String tieuDe,
        String moTa,
        String loai, // VIDEO | PDF | DOC | QUIZ | LINK | ZIP
        Float viTri,
        boolean anHien,
        boolean xemThu,
        String duongDan,
        Map<String, Object> metadata,
        boolean coBaiKiemTra,
        Boolean hoanThanh, // null nếu không truy vấn theo người dùng cụ thể (vd. giảng viên xem quản lý)
        /** Vị trí dừng (giây với VIDEO, số trang với PDF/DOC) — null nếu chưa từng xem hoặc không truy vấn theo người dùng. */
        Long viTriDung
) {
}
