package com.elearning.dinhDanhPhanQuyen.ungDung;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDangKy;

/**
 * Interface PUBLIC — module khác (nếu cần, hiếm khi) chỉ thấy được hợp
 * đồng này, không thấy DangKyDichVuImpl hay NguoiDungKho.
 */
public interface DangKyDichVu {

    /**
     * Đăng ký tài khoản mới. Tài khoản được tạo ở trạng thái CHO_XAC_NHAN
     * (UC-01) và một email kích hoạt được gửi qua AdapterGuiEmail.
     */
    void dangKy(YeuCauDangKy yeuCau);

    /**
     * Xác nhận token kích hoạt từ email, chuyển trạng thái tài khoản
     * sang HOAT_DONG nếu token còn hiệu lực.
     */
    void kichHoatTaiKhoan(String token);

    /**
     * Gửi lại email kích hoạt (KB-DD-03) — giới hạn 3 lần/giờ trên cùng một
     * email để chống spam hộp thư người khác. Không tiết lộ email có tồn
     * tại trong hệ thống hay không (cùng nguyên tắc với quên mật khẩu):
     * nếu email chưa đăng ký, hàm âm thầm không làm gì nhưng vẫn tính vào
     * giới hạn 3 lần/giờ.
     */
    void guiLaiMaKichHoat(String email);
}