package com.elearning.thanhToan.giaoDien;

import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiGiaoDichDoanhThu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiHoanTienChiTiet;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiPhieuDoiSoat;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTaiChinhTongQuan;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTongHopDoiSoatTheoGiangVien;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTongQuanDoanhThu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiXuHuongDoanhThu;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauCapNhatPhieuDoiSoat;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauXuLyHoanTien;
import com.elearning.thanhToan.ungDung.TaiChinhDichVu;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/tai-chinh")
public class TaiChinhController {

    private final TaiChinhDichVu taiChinhDichVu;

    @GetMapping("/tong-quan")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<PhanHoiTaiChinhTongQuan> layTongQuan() {
        return ResponseEntity.ok(taiChinhDichVu.layTongQuanTaiChinh());
    }

    @GetMapping("/doi-soat")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<Page<PhanHoiPhieuDoiSoat>> layDanhSach(
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String timKiem,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @PageableDefault(size = 10, sort = "ngayTao", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(taiChinhDichVu.layDanhSachPhieuDoiSoat(trangThai, timKiem, tuNgay, denNgay, pageable));
    }

    /** Export bắt buộc phải có — xuất TOÀN BỘ kết quả đang lọc (không chỉ trang hiện tại). */
    @GetMapping("/doi-soat/xuat-csv")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<byte[]> xuatCsvDoiSoat(
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String timKiem,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        List<PhanHoiPhieuDoiSoat> ds = taiChinhDichVu.layTatCaPhieuDoiSoatDaLoc(trangThai, timKiem, tuNgay, denNgay);
        byte[] csv = CsvDoiSoat.xuat(ds);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"doi-soat.csv\"")
                .body(csv);
    }

    @GetMapping("/doi-soat/theo-giang-vien")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<List<PhanHoiTongHopDoiSoatTheoGiangVien>> layTongHopTheoGiangVien(
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String timKiem,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return ResponseEntity.ok(taiChinhDichVu.layTongHopDoiSoatTheoGiangVien(trangThai, timKiem, tuNgay, denNgay));
    }

    /** Xem chi tiết từng phiếu của 1 giảng viên — dùng cùng bộ lọc với báo cáo tổng hợp phía trên. */
    @GetMapping("/doi-soat/theo-giang-vien/{maGiangVien}/chi-tiet")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<List<PhanHoiPhieuDoiSoat>> layChiTietTheoGiangVien(
            @PathVariable Long maGiangVien,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return ResponseEntity.ok(
                taiChinhDichVu.layChiTietPhieuDoiSoatTheoGiangVien(maGiangVien, trangThai, tuNgay, denNgay));
    }

    @PatchMapping("/doi-soat/{maDoiSoat}")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<PhanHoiPhieuDoiSoat> capNhatTrangThai(
            @PathVariable Long maDoiSoat,
            @Valid @RequestBody YeuCauCapNhatPhieuDoiSoat yeuCau) {
        return ResponseEntity.ok(taiChinhDichVu.capNhatPhieuDoiSoat(maDoiSoat, yeuCau));
    }

    /** Thao tác hàng loạt (Bulk Actions) trên nhiều dòng đã chọn trong bảng đối soát. */
    @PatchMapping("/doi-soat/hang-loat")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<List<PhanHoiPhieuDoiSoat>> capNhatHangLoat(
            @RequestBody YeuCauCapNhatHangLoatDoiSoat yeuCau) {
        return ResponseEntity.ok(
                taiChinhDichVu.capNhatHangLoatPhieuDoiSoat(yeuCau.dsMaDoiSoat(), yeuCau.capNhat()));
    }

    /** Drill-down (Bóc tách chi tiết) — các giao dịch gốc đã được gộp vào một phiếu đối soát. */
    @GetMapping("/doi-soat/{maDoiSoat}/giao-dich")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<List<PhanHoiGiaoDichDoanhThu>> layGiaoDichTheoDoiSoat(@PathVariable Long maDoiSoat) {
        return ResponseEntity.ok(taiChinhDichVu.layGiaoDichTheoDoiSoat(maDoiSoat));
    }

    public record YeuCauCapNhatHangLoatDoiSoat(List<Long> dsMaDoiSoat, YeuCauCapNhatPhieuDoiSoat capNhat) {
    }

    // ────────────────────────── Duyệt hoàn tiền — Khóa học ──────────────────────────

    @GetMapping("/hoan-tien/khoa-hoc")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<Page<PhanHoiHoanTienChiTiet>> layDanhSachHoanTienKhoaHoc(
            @RequestParam(required = false) String trangThai,
            @PageableDefault(size = 10, sort = "ngayTao", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(taiChinhDichVu.layDanhSachHoanTienKhoaHoc(trangThai, pageable));
    }

    @PatchMapping("/hoan-tien/khoa-hoc/{maHoanTien}")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<PhanHoiHoanTienChiTiet> xuLyHoanTienKhoaHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maHoanTien,
            @Valid @RequestBody YeuCauXuLyHoanTien yeuCau) {
        return ResponseEntity.ok(taiChinhDichVu.xuLyHoanTienKhoaHoc(maHoanTien, nd.getMaNguoiDung(), yeuCau));
    }

    // ────────────────────────── Duyệt hoàn tiền — Lớp học ──────────────────────────

    @GetMapping("/hoan-tien/lop-hoc")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<Page<PhanHoiHoanTienChiTiet>> layDanhSachHoanTienLopHoc(
            @RequestParam(required = false) String trangThai,
            @PageableDefault(size = 10, sort = "ngayTao", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(taiChinhDichVu.layDanhSachHoanTienLopHoc(trangThai, pageable));
    }

    @PatchMapping("/hoan-tien/lop-hoc/{maHoanTien}")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<PhanHoiHoanTienChiTiet> xuLyHoanTienLopHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maHoanTien,
            @Valid @RequestBody YeuCauXuLyHoanTien yeuCau) {
        return ResponseEntity.ok(taiChinhDichVu.xuLyHoanTienLopHoc(maHoanTien, nd.getMaNguoiDung(), yeuCau));
    }

    // ────────────────────────── UC-16: Lọc / biểu đồ / xuất CSV doanh thu toàn hệ thống ──────────────────────────

    @GetMapping("/doanh-thu/tong-quan")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<PhanHoiTongQuanDoanhThu> layTongQuanDoanhThuHeThong(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false) Long maKhoaHoc,
            @RequestParam(required = false) Long maGiangVien) {
        return ResponseEntity.ok(
                taiChinhDichVu.layTongQuanDoanhThuHeThong(tuNgay, denNgay, maKhoaHoc, maGiangVien));
    }

    @GetMapping("/doanh-thu/giao-dich")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<List<PhanHoiGiaoDichDoanhThu>> layGiaoDichDoanhThuHeThong(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false) Long maKhoaHoc,
            @RequestParam(required = false) Long maGiangVien) {
        return ResponseEntity.ok(
                taiChinhDichVu.layGiaoDichDoanhThuHeThong(tuNgay, denNgay, maKhoaHoc, maGiangVien));
    }

    @GetMapping("/doanh-thu/xu-huong")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<PhanHoiXuHuongDoanhThu> layXuHuongDoanhThuHeThong(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false) Long maKhoaHoc,
            @RequestParam(required = false) Long maGiangVien) {
        return ResponseEntity.ok(
                taiChinhDichVu.layXuHuongDoanhThuHeThong(tuNgay, denNgay, maKhoaHoc, maGiangVien));
    }

    @GetMapping("/doanh-thu/xuat-csv")
    @PreAuthorize("hasAnyRole('QTV_CAO_CAP', 'QTV_TAI_CHINH')")
    public ResponseEntity<byte[]> xuatCsvDoanhThuHeThong(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false) Long maKhoaHoc,
            @RequestParam(required = false) Long maGiangVien) {
        List<PhanHoiGiaoDichDoanhThu> ds =
                taiChinhDichVu.layGiaoDichDoanhThuHeThong(tuNgay, denNgay, maKhoaHoc, maGiangVien);
        byte[] csv = CsvDoanhThu.xuat(ds, true);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"doanh-thu-he-thong.csv\"")
                .body(csv);
    }
}