package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record YeuCauDangKy(

        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên không vượt quá 100 ký tự")
        String hoTen,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
        String matKhau,

        // Tùy chọn — khớp với cột sdt VARCHAR(15) không NOT NULL trong V1.
        // Cho phép null/rỗng, nhưng nếu có giá trị thì phải đúng định dạng
        // số điện thoại Việt Nam (10 số, bắt đầu bằng 0).
        @Pattern(regexp = "^$|^0[0-9]{9}$", message = "Số điện thoại phải có 10 số và bắt đầu bằng 0")
        String sdt
) {
}
