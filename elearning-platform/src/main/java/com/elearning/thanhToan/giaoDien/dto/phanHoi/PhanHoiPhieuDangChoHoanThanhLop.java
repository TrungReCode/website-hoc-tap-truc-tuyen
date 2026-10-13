package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Một giao dịch phí lớp học đã hết hạn giữ tiền nhưng CHƯA được lập phiếu
 * đối soát vì lớp học chưa hoàn thành chương trình dạy (xem
 * BoXuLyDoiSoatTuDong.daHoanThanhChuongTrinhDay) — hiển thị minh bạch cho
 * giảng viên biết vì sao khoản này chưa được giải ngân.
 */
public record PhanHoiPhieuDangChoHoanThanhLop(
        Long maPhieu,
        Long maLopHoc,
        String maLop,
        String tenKhoaHoc,
        BigDecimal soTien,
        String trangThaiLop,
        LocalDateTime ngayKetThucLop
) {
}