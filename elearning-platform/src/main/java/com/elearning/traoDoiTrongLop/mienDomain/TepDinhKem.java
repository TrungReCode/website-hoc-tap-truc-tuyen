package com.elearning.traoDoiTrongLop.mienDomain;

/**
 * Value object nhúng vào JSONB của bai_dang.tep_dinh_kem.
 * Không có bảng riêng — dữ liệu lưu trực tiếp trong cột JSONB.
 */
public record TepDinhKem(
        String ten,
        String duongDan,
        String loai   // "PDF", "IMAGE", "VIDEO", "OTHER"
) {}
