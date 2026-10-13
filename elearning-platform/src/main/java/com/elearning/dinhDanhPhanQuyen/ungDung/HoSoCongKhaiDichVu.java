package com.elearning.dinhDanhPhanQuyen.ungDung;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Interface PUBLIC duy nhất mà các module nghiệp vụ khác (choKhoaHoc,
 * lopHocLMS, traoDoiTrongLop...) được phép gọi vào module dinhDanhPhanQuyen
 * để lấy thông tin hiển thị cơ bản của người dùng theo mã. Đây là điểm
 * giao tiếp một chiều: dinhDanhPhanQuyen không bao giờ gọi ngược lại các
 * module này, nên không phát sinh circular dependency.
 */
public interface HoSoCongKhaiDichVu {

    Optional<ThongTinNguoiDungCoBan> layThongTinCoBan(Long maNguoiDung);

    /** Tra cứu hàng loạt — tránh N+1 khi hiển thị danh sách (vd. danh sách đánh giá). */
    Map<Long, ThongTinNguoiDungCoBan> layThongTinCoBanNhieu(Collection<Long> dsMaNguoiDung);

    /**
     * Tra cứu email — CHỈ dùng cho ngữ cảnh có đặc quyền hợp lệ (vd. giảng
     * viên xem danh sách học viên của lớp mình phụ trách để liên hệ). Tách
     * riêng khỏi ThongTinNguoiDungCoBan để không vô tình làm lộ email ở các
     * chỗ hiển thị công khai khác (đánh giá, bài đăng diễn đàn...).
     */
    Optional<String> layEmail(Long maNguoiDung);

    /** Tra cứu hàng loạt, cùng lý do như layEmail(Long). */
    Map<Long, String> layEmailNhieu(Collection<Long> dsMaNguoiDung);
}
