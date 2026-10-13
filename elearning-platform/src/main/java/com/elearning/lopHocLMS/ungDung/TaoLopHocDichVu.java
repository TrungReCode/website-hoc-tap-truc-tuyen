package com.elearning.lopHocLMS.ungDung;

import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauTaoLop;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * PUBLIC — UC-09 (phần tạo lớp). Giảng viên hợp lệ là người sở hữu khóa
 * học (tác giả) HOẶC đang giữ Giấy phép Giảng dạy (DAY_HOC) cho khóa học
 * đó — không nhất thiết phải là tác giả.
 */
public interface TaoLopHocDichVu {

    PhanHoiLop taoLop(Long maNguoiDung, YeuCauTaoLop yeuCau);

    List<PhanHoiLop> layLopCuaGiangVien(Long maNguoiDung);

    PhanHoiLop layChiTietChoGiangVien(Long maNguoiDung, Long maLopHoc);

    /** Chỉ xóa được khi lớp chưa có thành viên nào tham gia. */
    void xoaLop(Long maNguoiDung, Long maLopHoc);

    /**
     * Kiểm tra nhanh không throw exception — dùng bởi traoDoiTrongLop
     * để phân quyền mà không cần import LopHocKho xuyên module.
     */
    boolean laGiangVienCuaLop(Long maNguoiDung, Long maLopHoc);

    /**
     * Danh sách toàn bộ lớp học cho trang quản lý của QTV Nội dung — lọc theo trạng thái/mã lớp (tùy chọn). */
    Page<PhanHoiLop> layDanhSachChoAdmin(TrangThaiLop trangThai, String maLop, Pageable pageable);

    /**
     * PUBLIC — không cần đăng nhập. Danh sách lớp học đang hoạt động
     * (congKhai=true, trạng thái SAP_DIEN_RA hoặc HOAT_DONG) cho trang
     * "Danh sách lớp học đang hoạt động", tương tự Chợ Khóa Học nhưng ở
     * cấp độ lớp học cụ thể (đã có giảng viên, ngày khai giảng, sĩ số...).
     * maKhoaHoc/tuKhoa/gia đều optional dùng để lọc thêm.
     */
    Page<PhanHoiLop> layDanhSachCongKhai(Long maKhoaHoc, String tuKhoa, String gia, Pageable pageable);

    /**
     * Danh sách các lớp học (của bất kỳ giảng viên nào — có thể là chính tác
     * giả hoặc người khác đang giữ Giấy phép Giảng dạy) đang được xây dựng
     * dựa trên một khóa học cụ thể. Chỉ tác giả của khóa học đó mới được xem.
     */
    List<PhanHoiLop> layLopTheoKhoaHocChoTacGia(Long maNguoiDungTacGia, Long maKhoaHoc);
}