package com.elearning.traoDoiTrongLop.mienDomain.enumDomain;

/**
 * Phân loại bài đăng trong lớp học — khớp UC-09 (học viên)/UC-10 (giảng viên)
 * trong tài liệu use case chi tiết.
 *
 * THONG_BAO : giảng viên đăng thông báo cho lớp (chỉ giảng viên tạo được).
 * BAI_TAP   : giảng viên giao bài tập cho lớp (chỉ giảng viên tạo được).
 * HOI_DAP   : học viên hoặc giảng viên đặt câu hỏi, có thể được đánh dấu "đã giải đáp".
 * TAI_LIEU  : chia sẻ tài liệu hỗ trợ/link bổ sung (dạng bài đăng có tep_dinh_kem).
 * THAO_LUAN : mở chủ đề thảo luận chuyên đề, không có cơ chế "đã giải đáp".
 */
public enum LoaiBaiDang {
    THONG_BAO,
    BAI_TAP,
    HOI_DAP,
    TAI_LIEU,
    THAO_LUAN
}