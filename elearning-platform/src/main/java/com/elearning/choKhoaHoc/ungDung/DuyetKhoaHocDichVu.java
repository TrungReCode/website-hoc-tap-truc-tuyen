package com.elearning.choKhoaHoc.ungDung;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhSachKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauDuyetKhoaHoc;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.TrangThaiKhoaHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PUBLIC — UC-11 Kiểm duyệt nội dung, dành cho Quản trị viên nội dung.
 * Dùng Redis Distributed Lock để chống xung đột khi hai Admin cùng mở
 * một hồ sơ chờ duyệt (luồng ngoại lệ trong đặc tả use case).
 */
public interface DuyetKhoaHocDichVu {

    /**
     * Danh sách khóa học cho trang quản lý nội dung của Admin.
     * trangThai = null nghĩa là xem TẤT CẢ trạng thái (không chỉ riêng
     * CHO_KIEM_DUYET như trước đây); tuKhoa = null/rỗng nghĩa là không lọc theo tiêu đề.
     */
    Page<PhanHoiDanhSachKhoaHoc> layDanhSachQuanLy(TrangThaiKhoaHoc trangThai, String tuKhoa, Pageable pageable);

    /** Mở hồ sơ để xét duyệt — chiếm khóa Redis trong thời gian giới hạn để Admin khác không thể mở cùng lúc. */
    PhanHoiKhoaHoc moHoSoKiemDuyet(Long maNguoiDungAdmin, Long maKhoaHoc);

    /** Đóng hồ sơ mà không quyết định (rời màn hình) — nhả khóa Redis sớm. */
    void dongHoSoKiemDuyet(Long maNguoiDungAdmin, Long maKhoaHoc);

    /** Duyệt hoặc từ chối — chỉ thành công nếu Admin gọi đang giữ khóa hồ sơ này. */
    PhanHoiKhoaHoc duyetKhoaHoc(Long maNguoiDungAdmin, Long maKhoaHoc, YeuCauDuyetKhoaHoc yeuCau);
}