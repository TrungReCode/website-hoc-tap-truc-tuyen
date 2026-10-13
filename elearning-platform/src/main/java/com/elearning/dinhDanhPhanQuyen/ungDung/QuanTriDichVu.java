package com.elearning.dinhDanhPhanQuyen.ungDung;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiNguoiDungAdmin;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.ThongKeHeThong;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauCapLaiQuyenQuanTri;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauCapNhatTrangThai;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauTaoTaiKhoanAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PUBLIC — Nghiệp vụ quản trị hệ thống.
 * Chỉ được gọi từ AdminController, bảo vệ bằng @PreAuthorize ở tầng controller.
 */
public interface QuanTriDichVu {

    // QTV_CAO_CAP: quan sát toàn hệ thống
    ThongKeHeThong layThongKeHeThong();

    // QTV_CAO_CAP: quản lý tài khoản admin
    PhanHoiNguoiDungAdmin taoTaiKhoanAdmin(Long maNguoiDungCaoCap, YeuCauTaoTaiKhoanAdmin yeuCau);

    // QTV_CAO_CAP + QTV_NOI_DUNG: duyệt danh sách người dùng
    Page<PhanHoiNguoiDungAdmin> layDanhSachNguoiDung(String timKiem, String vaiTro, String trangThai, Pageable pageable);

    // QTV_CAO_CAP + QTV_NOI_DUNG: khoá/mở khoá tài khoản.
    // nguoiGoiLaCaoCap: QTV_NOI_DUNG chỉ được thao tác trên NGUOI_DUNG thường
    // (như trước giờ); riêng QTV_CAO_CAP còn được khoá thêm QTV_NOI_DUNG và
    // QTV_TAI_CHINH (không khoá được QTV_CAO_CAP khác, không tự khoá chính
    // mình — xem QuanTriDichVuImpl để biết chi tiết từng trường hợp).
    // Dùng boolean nguyên thủy thay vì truyền thẳng VaiTro/Entity NguoiDung
    // vì interface này còn được TaiChinhDichVuImpl (module thanhToan) import
    // — module khác không được phép biết Entity/enum nội bộ của
    // dinhDanhPhanQuyen.
    PhanHoiNguoiDungAdmin capNhatTrangThaiNguoiDung(Long maNguoiDungAdmin, boolean nguoiGoiLaCaoCap,
            Long maNguoiDung, YeuCauCapNhatTrangThai yeuCau);

    // QTV_CAO_CAP: xem chi tiết bất kỳ tài khoản
    PhanHoiNguoiDungAdmin layChiTietNguoiDung(Long maNguoiDung);

    /**
     * QTV_CAO_CAP: thu hồi quyền Quản trị viên — hạ cấp một tài khoản đang
     * là QTV_NOI_DUNG hoặc QTV_TAI_CHINH về vai trò NGUOI_DUNG thông thường.
     * Không áp dụng cho tài khoản QTV_CAO_CAP khác (tự bảo vệ khỏi việc một
     * QTV_CAO_CAP hạ cấp/khóa lẫn nhau qua API này).
     */
    PhanHoiNguoiDungAdmin thuHoiQuyenQuanTri(Long maNguoiDungCaoCap, Long maNguoiDungMucTieu);

    /**
     * QTV_CAO_CAP: cấp lại quyền Quản trị viên cho một tài khoản đang ở vai
     * trò NGUOI_DUNG thông thường — kể cả tài khoản từng bị thu hồi quyền
     * trước đó. Đối xứng với thuHoiQuyenQuanTri(); không dùng để tạo tài
     * khoản mới (xem taoTaiKhoanAdmin cho việc đó).
     */
    PhanHoiNguoiDungAdmin capLaiQuyenQuanTri(Long maNguoiDungCaoCap, Long maNguoiDungMucTieu, YeuCauCapLaiQuyenQuanTri yeuCau);
}