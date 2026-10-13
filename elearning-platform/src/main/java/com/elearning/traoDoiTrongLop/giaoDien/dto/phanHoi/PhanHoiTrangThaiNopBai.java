package com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Trạng thái nộp bài của MỘT học viên trong lớp đối với một bài đăng loại
 * BAI_TAP — dùng để giảng viên xem nhanh ai đã nộp / ai chưa nộp.
 *
 * Được tổng hợp hoàn toàn từ dữ liệu đã có (danh sách thành viên lớp ở
 * module lopHocLMS + các bình luận NOP_BAI/CHAM_DIEM ở chính bảng
 * binh_luan), KHÔNG cần thêm bảng/cột CSDL mới.
 */
public record PhanHoiTrangThaiNopBai(

        Long maHocVien,
        String tenHocVien,
        String anhDaiDien,

        /** "CHUA_NOP" | "DA_NOP" | "DA_CHAM". */
        String trangThai,

        /** Thời điểm nộp bài gần nhất — null nếu chưa nộp. */
        LocalDateTime ngayNop,

        /** Mã bình luận của bài nộp (dùng để gọi API chấm điểm) — null nếu chưa nộp. */
        Long maBinhLuanBaiNop,

        /** Chỉ có giá trị khi trangThai = "DA_CHAM". */
        BigDecimal diem,
        BigDecimal diemToiDa
) {}
