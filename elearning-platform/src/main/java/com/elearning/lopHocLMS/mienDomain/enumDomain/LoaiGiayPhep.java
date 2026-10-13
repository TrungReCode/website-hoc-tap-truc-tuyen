package com.elearning.lopHocLMS.mienDomain.enumDomain;

/**
 * Khớp quyen_su_dung.loai_giay_phep (VARCHAR(20), không phải Postgres native enum).
 * PUBLIC vì module thanhToan cần biết loại giấy phép (CA_NHAN dùng khoa_hoc.gia,
 * DAY_HOC dùng khoa_hoc.phi_day_hoc) khi xử lý giao dịch mua khóa học.
 */
public enum LoaiGiayPhep {
    CA_NHAN, // chỉ tự học, không được mở lớp
    DAY_HOC  // được mở lớp dạy lại nội dung khóa học
}
