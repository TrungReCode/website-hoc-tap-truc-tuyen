package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotNull;

/**
 * QTV_CAO_CAP cấp lại quyền quản trị cho một tài khoản đang ở vai trò
 * NGUOI_DUNG thông thường — kể cả tài khoản từng bị thu hồi quyền trước đó
 * qua thuHoiQuyenQuanTri(). Đối xứng với YeuCauTaoTaiKhoanAdmin nhưng không
 * cần hoTen/email/matKhau vì tài khoản đã tồn tại từ trước.
 */
public record YeuCauCapLaiQuyenQuanTri(

        /** Chỉ chấp nhận: QTV_NOI_DUNG, QTV_TAI_CHINH */
        @NotNull(message = "Vai trò không được để trống")
        String vaiTro
) {}
