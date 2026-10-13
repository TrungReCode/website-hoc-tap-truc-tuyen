package com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain;

/**
 * Khớp xac_thuc_giang_vien.trang_thai (VARCHAR(20), không phải Postgres
 * native enum — xem giải thích trong V16 migration).
 *
 * KHÔNG có giá trị "CHUA_GUI" — nếu người dùng chưa từng gửi yêu cầu thì
 * đơn giản là chưa tồn tại bản ghi XacThucGiangVien nào (Optional.empty()),
 * không cần một trạng thái riêng để biểu diễn "không có gì".
 */
public enum TrangThaiXacThucGiangVien {
    CHO_DUYET,
    DA_DUYET,
    TU_CHOI
}
