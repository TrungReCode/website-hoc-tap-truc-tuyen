package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau;

import jakarta.validation.constraints.NotBlank;

/** QTV_CAO_CAP hoặc QTV_NOI_DUNG khoá/mở tài khoản người dùng. */
public record YeuCauCapNhatTrangThai(

        @NotBlank(message = "Trạng thái không được để trống")
        String trangThai,  // "HOAT_DONG" | "BI_CAM"

        // Bắt buộc khi trangThai = "BI_CAM" (Service tự kiểm tra) — dùng để
        // gửi thông báo vi phạm cho người dùng và ghi vào audit log.
        String lyDo
) {}
