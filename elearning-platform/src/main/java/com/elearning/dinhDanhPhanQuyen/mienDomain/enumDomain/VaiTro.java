package com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain;

/**
 * Khớp với vai_tro_enum trong V1__tao_bang_nguoi_dung.sql.
 * PUBLIC vì các module khác (choKhoaHoc, thanhToan...) cần kiểm tra vai
 * trò để phân quyền (ví dụ: chỉ QTV_NOI_DUNG mới duyệt được khóa học).
 */
public enum VaiTro {
    QTV_CAO_CAP,
    QTV_NOI_DUNG,
    QTV_TAI_CHINH,
    NGUOI_DUNG
}
