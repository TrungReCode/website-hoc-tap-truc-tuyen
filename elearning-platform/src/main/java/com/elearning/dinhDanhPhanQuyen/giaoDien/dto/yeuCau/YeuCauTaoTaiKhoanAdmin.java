package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * QTV_CAO_CAP tạo tài khoản admin khác (QTV_NOI_DUNG hoặc QTV_TAI_CHINH).
 * Tài khoản được kích hoạt ngay (HOAT_DONG) mà không cần xác nhận email,
 * vì đây là tài khoản nội bộ do admin cấp phát.
 */
public record YeuCauTaoTaiKhoanAdmin(

        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100)
        String hoTen,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mật khẩu tạm thời không được để trống")
        @Size(min = 8, message = "Mật khẩu tối thiểu 8 ký tự")
        String matKhau,

        /** Chỉ chấp nhận: QTV_NOI_DUNG, QTV_TAI_CHINH */
        @NotNull(message = "Vai trò không được để trống")
        String vaiTro
) {}
