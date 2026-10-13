package com.elearning.lopHocLMS.ungDung;

import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLoiMoi;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLop;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiThanhVienLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauMoiHocVien;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauThamGiaBangMa;

import java.util.List;

/**
 * PUBLIC — UC-06 Tham gia lớp học, cơ chế Khóa kép:
 * Khóa 1 = đã sở hữu khóa học gốc (bảng quyen_su_dung, lopHocLMS tự quản);
 * Khóa 2 = đã đóng phí lớp học nếu lop_hoc.phiLopHoc > 0 (module thanhToan
 * xử lý qua phieu_tra_phi_lop_hoc — xem ThanhToanDichVuImpl).
 */
public interface GhiDanhDichVu {

    /**
     * Khóa 1 dùng độc lập — cho phép module thanhToan xác nhận (và tự cấp
     * QuyenSuDung CA_NHAN nếu đủ điều kiện: khóa học miễn phí hoặc người
     * gọi chính là tác giả gốc) quyền sở hữu khóa học nền của một lớp học,
     * TRƯỚC KHI tạo đơn thanh toán phí lớp học hoặc cấp quyền truy cập lớp.
     *
     * Bắt buộc phải gọi từ ThanhToanDichVuImpl.taoDonPhiLopHoc() (và lặp
     * lại khi xử lý IPN thành công) — nếu không, học viên có thể đóng phí
     * lớp học và được cấp ThanhVienLop mà chưa từng mua khóa học gốc, vì
     * luồng thanh toán không đi qua thucHienGhiDanh() nên không tự động
     * được Khóa 1 bảo vệ.
     *
     * Ném NgoaiLeNghiepVu (402, mã lỗi "CHUA_SO_HUU_KHOA_HOC_GOC") nếu
     * chưa đủ điều kiện. Không làm gì nếu lớp không gắn khóa học gốc hoặc
     * được mở với đặc quyền miễn Khóa 1 (xem TaoLopHocDichVuImpl).
     */
    void kiemTraKhoaThuNhat(Long maNguoiDung, Long maLopHoc);

    List<PhanHoiLoiMoi> moiHocVien(Long maNguoiDungGiangVien, Long maLopHoc, YeuCauMoiHocVien yeuCau);

    List<PhanHoiLoiMoi> layLoiMoiCuaLop(Long maNguoiDungGiangVien, Long maLopHoc);

    /** Lời mời đang chờ xác nhận gửi tới email của người dùng hiện tại. */
    List<PhanHoiLoiMoi> layLoiMoiCuaToi(Long maNguoiDung, String email);

    PhanHoiLop chapNhanLoiMoi(Long maNguoiDung, String email, Long maLoiMoi);

    void tuChoiLoiMoi(Long maNguoiDung, String email, Long maLoiMoi);

    /** Tham gia trực tiếp lớp công khai (congKhai=true) bằng mã lớp, không cần lời mời. */
    PhanHoiLop thamGiaBangMa(Long maNguoiDung, YeuCauThamGiaBangMa yeuCau);

    List<PhanHoiThanhVienLop> layThanhVien(Long maNguoiDungGiangVien, Long maLopHoc);

    List<PhanHoiLop> layLopCuaHocVien(Long maNguoiDung);

    boolean laThanhVien(Long maNguoiDung, Long maLopHoc);

    /**
     * Lấy danh sách thành viên mà KHÔNG kiểm tra quyền giảng viên.
     * Dùng nội bộ bởi module traoDoiTrongLop để gửi thông báo hàng loạt
     * mà không vi phạm quy ước ranh giới module.
     */
    List<PhanHoiThanhVienLop> layThanhVienCuaLop(Long maLopHoc);
}