package com.elearning.choKhoaHoc.mienDomain.enumDomain;

/**
 * Khớp với kiểu Postgres native enum "trang_thai_kh_enum" trong
 * V2__tao_bang_khoa_hoc.sql.
 *
 * PUBLIC vì module lopHocLMS cần kiểm tra một khóa học có đang ở trạng
 * thái CONG_KHAI hay không trước khi cho phép tạo lớp học dựa trên đó.
 */
public enum TrangThaiKhoaHoc {
    NHAP,            // Giảng viên đang soạn thảo, chưa gửi duyệt
    CHO_KIEM_DUYET,  // Đã gửi, đang chờ Quản trị viên nội dung xét duyệt
    CHO_BO_SUNG,     // QTV nội dung yêu cầu bổ sung — Giảng viên cần chỉnh sửa rồi gửi lại duyệt
    CONG_KHAI,       // Đã duyệt, hiển thị tại Chợ Khóa Học
    LUU_TRU,         // Giảng viên tự ẩn khỏi Chợ Khóa Học (không xóa dữ liệu)
    NOI_BO           // Bị Admin gỡ khỏi Chợ Khóa Học (vi phạm nội dung) nhưng học viên đã mua vẫn truy cập được
}
