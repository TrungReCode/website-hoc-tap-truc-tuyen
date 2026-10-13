package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Dữ liệu cho Màn Hình Tổng Quan doanh thu — KPI, biểu đồ tròn, cảnh báo bất thường. */
public record PhanHoiTongQuanDoanhThu(
        BigDecimal tongDoanhThuGhiNhan,   // tổng mọi giao dịch thành công trong kỳ
        BigDecimal doanhThuThucThu,       // phần đã giải ngân xong (tiền đã thực sự về tài khoản)
        BigDecimal doanhThuChoDoiSoat,    // phần còn lại — chưa đối soát hoặc đang xử lý
        long soLuongGiaoDich,
        List<PhanBoTheoLoai> phanBoTheoLoai,
        List<CanhBaoDoanhThu> canhBao
) {
    public record PhanBoTheoLoai(String loai, BigDecimal soTien, long soLuong) {
    }

    /** Một ngày có doanh thu đột biến so với trung bình các ngày còn lại trong kỳ đang xem. */
    public record CanhBaoDoanhThu(LocalDate ngay, BigDecimal soTien, double lanSoVoiTrungBinh) {
    }
}
