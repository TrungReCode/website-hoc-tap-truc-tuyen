package com.elearning.lopHocLMS.giaoDien.dto.phanHoi;

/**
 * Dùng riêng cho Quản trị viên xem nội dung Quiz để kiểm duyệt — CÓ
 * dapAnDung, khác hẳn PhanHoiDapAnAn (dùng khi học viên đang làm bài, cố ý
 * không có trường này để chống lộ đáp án). KHÔNG dùng DTO này cho bất kỳ
 * luồng nào liên quan tới học viên đang làm bài.
 */
public record PhanHoiDapAnChoAdmin(Integer id, String noiDung, Boolean dapAnDung) {
}
