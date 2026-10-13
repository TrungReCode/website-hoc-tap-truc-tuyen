package com.elearning.dinhDanhPhanQuyen.ungDung;

/**
 * Thông tin hiển thị công khai tối thiểu của một người dùng (KHÔNG bao
 * gồm email, sđt, vai trò hay bất kỳ trường nhạy cảm nào). Dùng để các
 * module khác (choKhoaHoc, lopHocLMS, traoDoiTrongLop...) hiển thị
 * "Giảng viên: Nguyễn Minh Tú" hoặc tên người đánh giá/người đăng bài mà
 * không cần biết đến Entity NguoiDung thật sự.
 */
public record ThongTinNguoiDungCoBan(
        Long maNguoiDung,
        String hoTen,
        String anhDaiDien
) {
}
