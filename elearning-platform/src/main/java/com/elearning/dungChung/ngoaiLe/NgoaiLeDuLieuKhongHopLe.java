package com.elearning.dungChung.ngoaiLe;

import org.springframework.http.HttpStatus;

/**
 * Dùng khi dữ liệu đầu vào không hợp lệ về mặt nghiệp vụ
 * (khác với lỗi validate format của @Valid — đây là lỗi logic, ví dụ:
 * email đã tồn tại, mật khẩu cũ không khớp, khóa học đang ở trạng thái
 * không cho phép sửa...).
 */
public class NgoaiLeDuLieuKhongHopLe extends NgoaiLeNghiepVu {

    public NgoaiLeDuLieuKhongHopLe(String thongBao) {
        super(thongBao, HttpStatus.BAD_REQUEST, "DU_LIEU_KHONG_HOP_LE");
    }
}
