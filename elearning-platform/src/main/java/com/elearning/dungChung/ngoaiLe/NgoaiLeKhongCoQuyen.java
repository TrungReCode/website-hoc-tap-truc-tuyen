package com.elearning.dungChung.ngoaiLe;

import org.springframework.http.HttpStatus;

/**
 * Dùng khi người dùng đã xác thực nhưng không có quyền thực hiện hành động.
 * Ví dụ: học viên cố gắng gọi API duyệt khóa học (chỉ Admin Nội Dung mới có quyền).
 */
public class NgoaiLeKhongCoQuyen extends NgoaiLeNghiepVu {

    public NgoaiLeKhongCoQuyen(String thongBao) {
        super(thongBao, HttpStatus.FORBIDDEN, "KHONG_CO_QUYEN");
    }

    public static NgoaiLeKhongCoQuyen macDinh() {
        return new NgoaiLeKhongCoQuyen("Bạn không có quyền thực hiện hành động này");
    }
}
