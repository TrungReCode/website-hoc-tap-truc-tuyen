package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import com.elearning.thanhToan.mienDomain.TrangThaiDoiSoat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Một dòng trong lịch sử giao dịch NHẬN tiền của người dùng (với vai trò
 * tác giả khóa học / giảng viên phụ trách lớp) — đối lập với
 * PhanHoiGiaoDich (giao dịch GỬI, vai trò người mua/người đóng phí).
 *
 * Tách riêng thành DTO khác vì: (1) trạng thái dùng TrangThaiDoiSoat, khác
 * hẳn TrangThaiGiaoDich của giao dịch gửi; (2) số tiền hiển thị là số thực
 * nhận SAU khi trừ hoa hồng nền tảng, không phải giá gốc học viên đã trả.
 */
public record PhanHoiGiaoDichNhan(
        String loaiGiaoDich,        // "KHOA_HOC" | "LOP_HOC"
        Long maDoiSoat,
        String tieuDe,
        Long maDoiTuong,            // maKhoaHoc hoặc maLopHoc
        BigDecimal soTienGiaoDichGoc, // số tiền học viên đã trả (trước hoa hồng)
        BigDecimal hoaHong,          // % hoa hồng nền tảng áp dụng cho giao dịch này
        BigDecimal soTienThucNhan,   // số tiền thực nhận về, sau khi trừ hoa hồng
        TrangThaiDoiSoat trangThai,
        LocalDateTime ngayTao,
        LocalDateTime ngayChuyenThanhCong // null nếu chưa giải ngân xong
) {
}
