package com.elearning.dinhDanhPhanQuyen.ungDung;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiDangNhap;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDangNhap;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDatLaiMatKhau;

public interface XacThucDichVu {

    PhanHoiDangNhap dangNhap(YeuCauDangNhap yeuCau);

    PhanHoiDangNhap lamMoiToken(String refreshToken);

    void dangXuat(String refreshToken);

    /**
     * UC quên mật khẩu — bước 1: gửi email chứa link đặt lại mật khẩu.
     * Luôn hoàn tất bình thường (không throw) kể cả khi email không tồn
     * tại trong hệ thống, để tránh lộ thông tin email nào đã đăng ký
     * (user enumeration).
     */
    void yeuCauDatLaiMatKhau(String email);

    /** UC quên mật khẩu — bước 2: xác thực token từ email và đặt mật khẩu mới. */
    void datLaiMatKhau(YeuCauDatLaiMatKhau yeuCau);
}