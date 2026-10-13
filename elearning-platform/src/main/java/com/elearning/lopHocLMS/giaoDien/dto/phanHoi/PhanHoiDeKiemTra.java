package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

import java.util.List;

public record PhanHoiDeKiemTra(
        String loaiNguon, // GOC | BO_SUNG
        Long maBaiKiemTra, // mã bai_kiem_tra (GOC) hoặc bai_kiem_tra_bo_sung (BO_SUNG)
        String tieuDe,
        Integer diemToiThieu,
        Integer thoiGian, // phút, tổng thời gian cho phép — null = không giới hạn
        /**
         * Số giây CÒN LẠI tính từ thời điểm bắt đầu làm bài đã lưu ở Redis
         * (xem KiemTraTrongLopDichVuImpl.tinhGiayConLai) — KHÔNG phải luôn
         * bằng thoiGian*60. Nếu học viên thoát ra vào lại, giá trị này giảm
         * dần theo thời gian thực đã trôi qua, chứ không reset về đầu.
         * Frontend PHẢI dùng field này để khởi tạo đồng hồ đếm ngược, không
         * được tự tính lại từ thoiGian*60. null = không giới hạn thời gian.
         */
        Integer giayConLai,
        List<PhanHoiCauHoiTrongLop> danhSachCauHoi
) {
}
