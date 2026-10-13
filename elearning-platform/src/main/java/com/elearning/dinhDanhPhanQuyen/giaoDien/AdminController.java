package com.elearning.dinhDanhPhanQuyen.giaoDien;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiCauHinhHeThong;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiNguoiDungAdmin;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.ThongKeHeThong;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauCapLaiQuyenQuanTri;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauCapNhatCauHinhHeThong;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauCapNhatTrangThai;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauTaoTaiKhoanAdmin;
import com.elearning.dinhDanhPhanQuyen.ungDung.CauHinhHeThongDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.QuanTriDichVu;
import com.elearning.dinhDanhPhanQuyen.mienDomain.NguoiDung;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.VaiTro;
import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import com.elearning.dungChung.nhatKy.NhatKyHeThongDichVu;
import com.elearning.dungChung.nhatKy.PhanHoiNhatKy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Nhóm endpoint /api/admin/** — bảo vệ bằng @PreAuthorize theo vai trò.
 *
 * Phân quyền:
 *   QTV_CAO_CAP   — toàn quyền: thống kê, tạo admin, thu hồi quyền admin,
 *                   cấu hình hệ thống, quản lý tất cả user
 *   QTV_NOI_DUNG  — duyệt khóa học (endpoint riêng ở choKhoaHoc module)
 *                   + xem & khoá tài khoản NGUOI_DUNG thông thường
 *   QTV_TAI_CHINH — xem thống kê tài chính + xử lý đối soát
 *                   (endpoint đối soát riêng ở thanhToan module)
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final QuanTriDichVu quanTriDichVu;
    private final CauHinhHeThongDichVu cauHinhHeThongDichVu;
    private final NhatKyHeThongDichVu nhatKyHeThongDichVu;

    // ═══════════════════════════════════════════
    //  QTV_CAO_CAP — Thống kê & quan sát hệ thống
    // ═══════════════════════════════════════════

    @GetMapping("/thong-ke")
    @PreAuthorize("hasRole('QTV_CAO_CAP')")
    public ResponseEntity<ThongKeHeThong> layThongKe() {
        return ResponseEntity.ok(quanTriDichVu.layThongKeHeThong());
    }

    // ═══════════════════════════════════════════
    //  QTV_CAO_CAP — Cấu hình thông số toàn cục
    // ═══════════════════════════════════════════

    @GetMapping("/cau-hinh-he-thong")
    @PreAuthorize("hasRole('QTV_CAO_CAP')")
    public ResponseEntity<PhanHoiCauHinhHeThong> layCauHinhHeThong() {
        return ResponseEntity.ok(cauHinhHeThongDichVu.layCauHinh());
    }

    @PutMapping("/cau-hinh-he-thong")
    @PreAuthorize("hasRole('QTV_CAO_CAP')")
    public ResponseEntity<PhanHoiCauHinhHeThong> capNhatCauHinhHeThong(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDungAdmin,
            @Valid @RequestBody YeuCauCapNhatCauHinhHeThong yeuCau) {
        return ResponseEntity.ok(
                cauHinhHeThongDichVu.capNhatCauHinh(nguoiDungAdmin.getMaNguoiDung(), yeuCau));
    }

    // ═══════════════════════════════════════════
    //  QTV_CAO_CAP — Quản lý tài khoản admin
    // ═══════════════════════════════════════════

    @PostMapping("/tai-khoan-admin")
    @PreAuthorize("hasRole('QTV_CAO_CAP')")
    public ResponseEntity<PhanHoiNguoiDungAdmin> taoTaiKhoanAdmin(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDungAdmin,
            @Valid @RequestBody YeuCauTaoTaiKhoanAdmin yeuCau) {
        return ResponseEntity.status(201)
                .body(quanTriDichVu.taoTaiKhoanAdmin(nguoiDungAdmin.getMaNguoiDung(), yeuCau));
    }

    @PostMapping("/tai-khoan-admin/{maNguoiDung}/thu-hoi")
    @PreAuthorize("hasRole('QTV_CAO_CAP')")
    public ResponseEntity<PhanHoiNguoiDungAdmin> thuHoiQuyenQuanTri(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDungAdmin,
            @PathVariable Long maNguoiDung) {
        return ResponseEntity.ok(
                quanTriDichVu.thuHoiQuyenQuanTri(nguoiDungAdmin.getMaNguoiDung(), maNguoiDung));
    }

    /**
     * Cấp lại quyền quản trị cho tài khoản đang ở NGUOI_DUNG — kể cả tài
     * khoản từng bị thu hồi quyền qua endpoint /thu-hoi ở trên. Đây là
     * endpoint bổ sung cho khoảng trống UC-19 chưa đặc tả nhánh "cấp lại
     * quyền", chỉ có "thu hồi quyền" và "khóa tài khoản".
     */
    @PostMapping("/tai-khoan-admin/{maNguoiDung}/cap-lai-quyen")
    @PreAuthorize("hasRole('QTV_CAO_CAP')")
    public ResponseEntity<PhanHoiNguoiDungAdmin> capLaiQuyenQuanTri(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDungAdmin,
            @PathVariable Long maNguoiDung,
            @Valid @RequestBody YeuCauCapLaiQuyenQuanTri yeuCau) {
        return ResponseEntity.ok(
                quanTriDichVu.capLaiQuyenQuanTri(nguoiDungAdmin.getMaNguoiDung(), maNguoiDung, yeuCau));
    }

    // ═══════════════════════════════════════════
    //  QTV_CAO_CAP + QTV_NOI_DUNG — Quản lý user
    // ═══════════════════════════════════════════

    @GetMapping("/nguoi-dung")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_NOI_DUNG')")
    public ResponseEntity<Page<PhanHoiNguoiDungAdmin>> layDanhSachNguoiDung(
            @RequestParam(required = false) String timKiem,
            @RequestParam(required = false) String vaiTro,
            @RequestParam(required = false) String trangThai,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                quanTriDichVu.layDanhSachNguoiDung(timKiem, vaiTro, trangThai, pageable));
    }

    @GetMapping("/nguoi-dung/{maNguoiDung}")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_NOI_DUNG')")
    public ResponseEntity<PhanHoiNguoiDungAdmin> layChiTiet(@PathVariable Long maNguoiDung) {
        return ResponseEntity.ok(quanTriDichVu.layChiTietNguoiDung(maNguoiDung));
    }

    @PatchMapping("/nguoi-dung/{maNguoiDung}/trang-thai")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_NOI_DUNG')")
    public ResponseEntity<PhanHoiNguoiDungAdmin> capNhatTrangThai(
            // Dùng thẳng Entity NguoiDung (không phải NguoiDungThamChieu) vì
            // Controller này nằm CÙNG module dinhDanhPhanQuyen với Entity —
            // không vi phạm ranh giới module nào. Cần biết getVaiTro() để
            // phân biệt QTV_CAO_CAP với QTV_NOI_DUNG (xem capNhatTrangThaiNguoiDung).
            @AuthenticationPrincipal NguoiDung nguoiDungAdmin,
            @PathVariable Long maNguoiDung,
            @Valid @RequestBody YeuCauCapNhatTrangThai yeuCau) {
        boolean nguoiGoiLaCaoCap = nguoiDungAdmin.getVaiTro() == VaiTro.QTV_CAO_CAP;
        return ResponseEntity.ok(quanTriDichVu.capNhatTrangThaiNguoiDung(
                nguoiDungAdmin.getMaNguoiDung(), nguoiGoiLaCaoCap, maNguoiDung, yeuCau));
    }

    // ═══════════════════════════════════════════
    //  QTV_CAO_CAP — Xem nhật ký hành động quản trị
    // ═══════════════════════════════════════════

    /**
     * Xem audit log (logs/audit.log hoặc logs/audit-{ngay}.log) — chỉ
     * QTV_CAO_CAP được xem, vì đây là nhật ký hành động của chính giới quản
     * trị (khóa tài khoản, thu hồi quyền, đổi cấu hình hệ thống...).
     *
     * @param ngay   ngày cần xem, mặc định hôm nay (định dạng yyyy-MM-dd)
     * @param soDong số dòng gần nhất, mặc định 200, tối đa 1000 (chặn ở service)
     * @param tuKhoa lọc theo từ khóa, vd "KHOA_TAI_KHOAN" hoặc "nguoi_dung#42"
     */
    @GetMapping("/nhat-ky")
    @PreAuthorize("hasRole('QTV_CAO_CAP')")
    public ResponseEntity<PhanHoiNhatKy> xemNhatKy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            @RequestParam(defaultValue = "200") int soDong,
            @RequestParam(required = false) String tuKhoa) {
        return ResponseEntity.ok(nhatKyHeThongDichVu.xemNhatKy(ngay, soDong, tuKhoa));
    }
}