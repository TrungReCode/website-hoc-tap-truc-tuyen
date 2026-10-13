package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import java.math.BigDecimal;
import java.util.List;

/**
 * Báo cáo đối soát TỔNG HỢP THEO GIẢNG VIÊN — trả lời câu hỏi "tổng cộng cần
 * chuyển cho giảng viên X bao nhiêu tiền trong kỳ đã chọn (cuối tháng hoặc
 * một khoảng thời gian tùy ý)".
 *
 * Được gộp từ các {@link PhanHoiPhieuDoiSoat} theo bộ lọc trạng thái/thời
 * gian mà Quản trị viên đang xem — KHÔNG tạo bảng/cột CSDL mới, chỉ tổng hợp
 * (group by) trong tầng ứng dụng từ dữ liệu phiếu đối soát đã có sẵn.
 *
 * {@code tongCanChuyen} và {@code dsMaDoiSoatChuaHoanTat} luôn chỉ tính trên
 * các phiếu CHƯA ở trạng thái HOAN_TAT — bất kể bộ lọc trạng thái đang hiển
 * thị là gì — để nút "Hoàn tất hàng loạt" luôn thao tác đúng trên số tiền
 * thực sự còn cần chuyển, không vô tình xử lý lại phiếu đã hoàn tất.
 */
public record PhanHoiTongHopDoiSoatTheoGiangVien(
        Long maGiangVien,
        String tenGiangVien,
        String tenChuTaiKhoan,
        String soTaiKhoan,
        String tenNganHang,

        /** Tổng số phiếu khớp bộ lọc hiện tại (mọi trạng thái đang được xem). */
        int soLuongPhieu,
        BigDecimal tongDoanhThu,
        BigDecimal tongSoTienThucTe,

        /** Số phiếu CHƯA hoàn tất trong tập đang lọc — dùng để quyết định có hiện nút hành động hay không. */
        int soLuongChuaHoanTat,

        /** Tổng số tiền THỰC SỰ CẦN CHUYỂN (chỉ tính phiếu chưa hoàn tất). */
        BigDecimal tongCanChuyen,

        /** Mã các phiếu chưa hoàn tất — truyền thẳng vào API hàng loạt sẵn có khi bấm "Hoàn tất hàng loạt". */
        List<Long> dsMaDoiSoatChuaHoanTat
) {}
