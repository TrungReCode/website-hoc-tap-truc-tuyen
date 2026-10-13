package com.elearning.dungChung.ngoaiLe;

import org.springframework.http.HttpStatus;

/**
 * Dùng khi không tìm thấy tài nguyên (entity) theo mã định danh.
 * Ví dụ: NguoiDungKho không tìm thấy user theo email khi đăng nhập.
 */
public class NgoaiLeKhongTimThay extends NgoaiLeNghiepVu {

    public NgoaiLeKhongTimThay(String thongBao) {
        super(thongBao, HttpStatus.NOT_FOUND, "KHONG_TIM_THAY");
    }

    public static NgoaiLeKhongTimThay cuaTaiNguyen(String tenTaiNguyen, Object maTaiNguyen) {
        return new NgoaiLeKhongTimThay(
                String.format("Không tìm thấy %s với mã: %s", tenTaiNguyen, maTaiNguyen));
    }
}
