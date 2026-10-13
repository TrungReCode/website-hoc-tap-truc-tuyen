package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi;

import java.time.LocalDateTime;

/** Thông tin người dùng đầy đủ dành cho các trang quản trị. */
public record PhanHoiNguoiDungAdmin(
        Long maNguoiDung,
        String hoTen,
        String email,
        String sdt,
        String vaiTro,
        String trangThai,
        String anhDaiDien,
        LocalDateTime ngayTao
) {}
