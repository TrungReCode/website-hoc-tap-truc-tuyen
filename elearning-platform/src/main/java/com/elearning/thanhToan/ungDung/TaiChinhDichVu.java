package com.elearning.thanhToan.ungDung;

import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiDoanhThuGiangVien;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiGiaoDichDoanhThu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiHoanTienChiTiet;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiPhieuDangChoHoanThanhLop;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiPhieuDoiSoat;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTaiChinhTongQuan;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTongHopDoiSoatTheoGiangVien;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTongQuanDoanhThu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiXuHuongDoanhThu;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauCapNhatPhieuDoiSoat;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauXuLyHoanTien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TaiChinhDichVu {

    PhanHoiTaiChinhTongQuan layTongQuanTaiChinh();

    Page<PhanHoiPhieuDoiSoat> layDanhSachPhieuDoiSoat(
            String trangThai, String timKiem, LocalDate tuNgay, LocalDate denNgay, Pageable pageable);

    /** Không phân trang — dùng để xuất CSV toàn bộ kết quả lọc (Export/Import bắt buộc phải có). */
    List<PhanHoiPhieuDoiSoat> layTatCaPhieuDoiSoatDaLoc(String trangThai, String timKiem, LocalDate tuNgay, LocalDate denNgay);

    PhanHoiPhieuDoiSoat capNhatPhieuDoiSoat(Long maDoiSoat, YeuCauCapNhatPhieuDoiSoat yeuCau);

    /** Thao tác hàng loạt (Bulk Actions) — áp dụng cùng một cập nhật trạng thái cho nhiều phiếu đối soát cùng lúc. */
    List<PhanHoiPhieuDoiSoat> capNhatHangLoatPhieuDoiSoat(List<Long> dsMaDoiSoat, YeuCauCapNhatPhieuDoiSoat yeuCau);

    /** Drill-down: danh sách giao dịch gốc (mua khóa học + phí lớp học) đã được gộp vào một phiếu đối soát. */
    List<PhanHoiGiaoDichDoanhThu> layGiaoDichTheoDoiSoat(Long maDoiSoat);

    /**
     * Báo cáo tổng hợp đối soát THEO GIẢNG VIÊN cho một kỳ (cuối tháng hoặc
     * khoảng thời gian tùy chọn) — mỗi dòng là tổng tiền cần chuyển cho một
     * giảng viên, thay vì xem từng phiếu lẻ. Cùng bộ lọc trạng thái/tìm
     * kiếm/thời gian như {@link #layDanhSachPhieuDoiSoat}.
     */
    List<PhanHoiTongHopDoiSoatTheoGiangVien> layTongHopDoiSoatTheoGiangVien(
            String trangThai, String timKiem, LocalDate tuNgay, LocalDate denNgay);

    /**
     * Xem chi tiết (drill-down) TOÀN BỘ phiếu đối soát của MỘT giảng viên cụ
     * thể trong báo cáo tổng hợp — dùng cùng bộ lọc trạng thái/thời gian mà
     * Quản trị viên đang xem, để danh sách chi tiết khớp đúng với con số
     * tổng đang hiển thị. Khác với {@link #layDanhSachPhieuDoiSoatCuaGiangVien}
     * (dành cho chính giảng viên tự xem, không lọc theo thời gian) — hàm này
     * cho phép Quản trị viên xem của BẤT KỲ giảng viên nào theo mã.
     */
    List<PhanHoiPhieuDoiSoat> layChiTietPhieuDoiSoatTheoGiangVien(
            Long maGiangVien, String trangThai, LocalDate tuNgay, LocalDate denNgay);

    /** UC-xx (giảng viên) — tổng quan doanh thu/đối soát của chính giảng viên đang đăng nhập. */
    PhanHoiDoanhThuGiangVien layDoanhThuCuaGiangVien(Long maGiangVien);

    /** UC-xx (giảng viên) — lịch sử phiếu đối soát của chính giảng viên, lọc theo trạng thái. */
    Page<PhanHoiPhieuDoiSoat> layDanhSachPhieuDoiSoatCuaGiangVien(Long maGiangVien, String trangThai, Pageable pageable);

    /**
     * Danh sách giao dịch phí lớp học đã hết hạn giữ tiền nhưng còn bị hoãn
     * lập phiếu đối soát vì lớp học chưa hoàn thành chương trình dạy — minh
     * bạch cho giảng viên biết lý do khoản thu chưa được giải ngân.
     */
    List<PhanHoiPhieuDangChoHoanThanhLop> layDanhSachDangChoHoanThanhLop(Long maGiangVien);

    /** UC duyệt hoàn tiền — danh sách yêu cầu hoàn tiền khóa học, lọc theo trạng thái (mặc định: tất cả). */
    Page<PhanHoiHoanTienChiTiet> layDanhSachHoanTienKhoaHoc(String trangThai, Pageable pageable);

    /** UC duyệt hoàn tiền — danh sách yêu cầu hoàn tiền lớp học, lọc theo trạng thái (mặc định: tất cả). */
    Page<PhanHoiHoanTienChiTiet> layDanhSachHoanTienLopHoc(String trangThai, Pageable pageable);

    /** Duyệt (HOAN_TAT) hoặc từ chối (TU_CHOI) một yêu cầu hoàn tiền khóa học. */
    PhanHoiHoanTienChiTiet xuLyHoanTienKhoaHoc(Long maHoanTien, Long maNguoiDungAdmin, YeuCauXuLyHoanTien yeuCau);

    /** Duyệt (HOAN_TAT) hoặc từ chối (TU_CHOI) một yêu cầu hoàn tiền lớp học. */
    PhanHoiHoanTienChiTiet xuLyHoanTienLopHoc(Long maHoanTien, Long maNguoiDungAdmin, YeuCauXuLyHoanTien yeuCau);

    // ────────────────────────── UC-16: Lọc / biểu đồ / xuất CSV doanh thu ──────────────────────────

    /**
     * Danh sách giao dịch doanh thu (mua khóa học + phí lớp học) của một
     * giảng viên, lọc theo khoảng thời gian và/hoặc khóa học cụ thể (mọi
     * tham số lọc đều tùy chọn — null nghĩa là không áp dụng). Dùng cho cả
     * hiển thị bảng lẫn xuất CSV.
     */
    List<PhanHoiGiaoDichDoanhThu> layGiaoDichDoanhThuGiangVien(
            Long maGiangVien, LocalDate tuNgay, LocalDate denNgay, Long maKhoaHoc);

    /** Xu hướng doanh thu theo tháng của một giảng viên, cùng bộ lọc như trên. */
    PhanHoiXuHuongDoanhThu layXuHuongDoanhThuGiangVien(
            Long maGiangVien, LocalDate tuNgay, LocalDate denNgay, Long maKhoaHoc);

    /**
     * Danh sách giao dịch doanh thu toàn hệ thống (QTV_CAO_CAP/QTV_TAI_CHINH)
     * — lọc theo thời gian, khóa học, và/hoặc một giảng viên cụ thể (mọi
     * tham số đều tùy chọn).
     */
    List<PhanHoiGiaoDichDoanhThu> layGiaoDichDoanhThuHeThong(
            LocalDate tuNgay, LocalDate denNgay, Long maKhoaHoc, Long maGiangVien);

    /** Xu hướng doanh thu theo tháng toàn hệ thống, cùng bộ lọc như trên. */
    PhanHoiXuHuongDoanhThu layXuHuongDoanhThuHeThong(
            LocalDate tuNgay, LocalDate denNgay, Long maKhoaHoc, Long maGiangVien);

    /** Màn Hình Tổng Quan — KPI dòng tiền + phân bổ theo loại + cảnh báo bất thường, của một giảng viên. */
    PhanHoiTongQuanDoanhThu layTongQuanDoanhThuGiangVien(Long maGiangVien, LocalDate tuNgay, LocalDate denNgay);

    /** Màn Hình Tổng Quan — cùng nội dung nhưng toàn hệ thống (QTV_CAO_CAP/QTV_TAI_CHINH), lọc thêm theo giảng viên. */
    PhanHoiTongQuanDoanhThu layTongQuanDoanhThuHeThong(
            LocalDate tuNgay, LocalDate denNgay, Long maKhoaHoc, Long maGiangVien);
}