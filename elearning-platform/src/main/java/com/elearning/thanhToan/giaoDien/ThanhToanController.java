package com.elearning.thanhToan.giaoDien;

import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiDoanhThuGiangVien;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiGiaoDich;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiGiaoDichDoanhThu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiGiaoDichNhan;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTongQuanDoanhThu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiXuHuongDoanhThu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiYeuCauHoanTien;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTaoDonThanhToan;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiPhieuDoiSoat;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiPhieuDangChoHoanThanhLop;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiXuLyVnpay;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauHoanTien;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauThanhToanKhoaHoc;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauThanhToanLopHoc;
import com.elearning.thanhToan.ungDung.TaiChinhDichVu;
import com.elearning.thanhToan.ungDung.ThanhToanDichVu;
import com.elearning.lopHocLMS.haTang.QuyenSuDungKho;
import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiGiayPhep;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/thanh-toan")
public class ThanhToanController {

    private final ThanhToanDichVu thanhToanDichVu;
    private final TaiChinhDichVu taiChinhDichVu;
    private final QuyenSuDungKho quyenSuDungKho;

    @GetMapping("/doanh-thu-cua-toi")
    public ResponseEntity<PhanHoiDoanhThuGiangVien> layDoanhThuCuaToi(@AuthenticationPrincipal NguoiDungThamChieu nd) {
        return ResponseEntity.ok(taiChinhDichVu.layDoanhThuCuaGiangVien(nd.getMaNguoiDung()));
    }

    @GetMapping("/doi-soat-cua-toi")
    public ResponseEntity<Page<PhanHoiPhieuDoiSoat>> layDoiSoatCuaToi(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @RequestParam(required = false) String trangThai,
            @PageableDefault(size = 10, sort = "ngayTao", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(taiChinhDichVu.layDanhSachPhieuDoiSoatCuaGiangVien(nd.getMaNguoiDung(), trangThai, pageable));
    }

    @GetMapping("/dang-cho-hoan-thanh-lop")
    public ResponseEntity<List<PhanHoiPhieuDangChoHoanThanhLop>> layDangChoHoanThanhLop(@AuthenticationPrincipal NguoiDungThamChieu nd) {
        return ResponseEntity.ok(taiChinhDichVu.layDanhSachDangChoHoanThanhLop(nd.getMaNguoiDung()));
    }

    // ─── UC-16: Lọc / biểu đồ / xuất CSV doanh thu (giảng viên xem của chính mình) ───

    @GetMapping("/doanh-thu-cua-toi/giao-dich")
    public ResponseEntity<List<PhanHoiGiaoDichDoanhThu>> layGiaoDichDoanhThuCuaToi(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false) Long maKhoaHoc) {
        return ResponseEntity.ok(
                taiChinhDichVu.layGiaoDichDoanhThuGiangVien(nd.getMaNguoiDung(), tuNgay, denNgay, maKhoaHoc));
    }

    @GetMapping("/doanh-thu-cua-toi/xu-huong")
    public ResponseEntity<PhanHoiXuHuongDoanhThu> layXuHuongDoanhThuCuaToi(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false) Long maKhoaHoc) {
        return ResponseEntity.ok(
                taiChinhDichVu.layXuHuongDoanhThuGiangVien(nd.getMaNguoiDung(), tuNgay, denNgay, maKhoaHoc));
    }

    @GetMapping("/doanh-thu-cua-toi/tong-quan")
    public ResponseEntity<PhanHoiTongQuanDoanhThu> layTongQuanDoanhThuCuaToi(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return ResponseEntity.ok(taiChinhDichVu.layTongQuanDoanhThuGiangVien(nd.getMaNguoiDung(), tuNgay, denNgay));
    }

    @GetMapping("/doanh-thu-cua-toi/xuat-csv")
    public ResponseEntity<byte[]> xuatCsvDoanhThuCuaToi(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false) Long maKhoaHoc) {
        List<PhanHoiGiaoDichDoanhThu> ds =
                taiChinhDichVu.layGiaoDichDoanhThuGiangVien(nd.getMaNguoiDung(), tuNgay, denNgay, maKhoaHoc);
        byte[] csv = CsvDoanhThu.xuat(ds, false);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"doanh-thu.csv\"")
                .body(csv);
    }

    @GetMapping("/kiem-tra-so-huu/{maKhoaHoc}")
    public ResponseEntity<Boolean> kiemTraSoHuu(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maKhoaHoc,
            @RequestParam LoaiGiayPhep loai) { // Thêm param loai

        if (nd == null)
            return ResponseEntity.ok(false);

        boolean daMua = quyenSuDungKho.existsByMaNguoiDungAndMaKhoaHocAndLoaiGiayPhep(nd.getMaNguoiDung(), maKhoaHoc,
                loai);
        return ResponseEntity.ok(daMua);
    }

    @PostMapping("/khoa-hoc/{maKhoaHoc}/vnpay")
    public ResponseEntity<PhanHoiTaoDonThanhToan> taoDonMuaKhoaHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maKhoaHoc,
            @Valid @RequestBody YeuCauThanhToanKhoaHoc yeuCau,
            @RequestHeader(value = "Origin", required = false) String origin) {
        return ResponseEntity.ok(thanhToanDichVu.taoDonMuaKhoaHoc(nd.getMaNguoiDung(), maKhoaHoc, yeuCau, origin));
    }

    @PostMapping("/lop-hoc/{maLop}/vnpay")
    public ResponseEntity<PhanHoiTaoDonThanhToan> taoDonPhiLopHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable String maLop, // Đổi Long thành String
            @Valid @RequestBody(required = false) YeuCauThanhToanLopHoc yeuCau,
            @RequestHeader(value = "Origin", required = false) String origin) {

        return ResponseEntity.ok(thanhToanDichVu.taoDonPhiLopHoc(
                nd.getMaNguoiDung(),
                maLop, // Truyền trực tiếp String vào service
                yeuCau == null ? new YeuCauThanhToanLopHoc() : yeuCau,
                origin));
    }

    @PostMapping("/hoan-tien/khoa-hoc/{maPhieuGiaoDich}")
    public ResponseEntity<PhanHoiYeuCauHoanTien> taoYeuCauHoanTienKhoaHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maPhieuGiaoDich,
            @Valid @RequestBody YeuCauHoanTien yeuCau) {
        return ResponseEntity
                .ok(thanhToanDichVu.taoYeuCauHoanTienKhoaHoc(nd.getMaNguoiDung(), maPhieuGiaoDich, yeuCau));
    }

    @PostMapping("/hoan-tien/lop-hoc/{maPhieuGiaoDich}")
    public ResponseEntity<PhanHoiYeuCauHoanTien> taoYeuCauHoanTienLopHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maPhieuGiaoDich,
            @Valid @RequestBody YeuCauHoanTien yeuCau) {
        return ResponseEntity.ok(thanhToanDichVu.taoYeuCauHoanTienLopHoc(nd.getMaNguoiDung(), maPhieuGiaoDich, yeuCau));
    }

    @GetMapping("/lich-su")
    public ResponseEntity<List<PhanHoiGiaoDich>> layLichSuGiaoDich(@AuthenticationPrincipal NguoiDungThamChieu nd) {
        return ResponseEntity.ok(thanhToanDichVu.layLichSuGiaoDich(nd.getMaNguoiDung()));
    }

    /** Lịch sử NHẬN tiền (vai trò tác giả khóa học/giảng viên phụ trách lớp) — bổ sung cho /lich-su (chỉ giao dịch gửi). */
    @GetMapping("/lich-su-nhan")
    public ResponseEntity<List<PhanHoiGiaoDichNhan>> layLichSuGiaoDichNhan(@AuthenticationPrincipal NguoiDungThamChieu nd) {
        return ResponseEntity.ok(thanhToanDichVu.layLichSuGiaoDichNhan(nd.getMaNguoiDung()));
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<PhanHoiXuLyVnpay> vnpayReturn(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(thanhToanDichVu.xuLyReturn(new HashMap<>(params)));
    }

    @PostMapping("/vnpay-ipn")
    public ResponseEntity<PhanHoiXuLyVnpay> vnpayIpn(
            @RequestParam Map<String, String> params,
            HttpServletRequest request) {
        Map<String, String> duLieu = new HashMap<>(params);
        duLieu.putIfAbsent("vnp_IpAddr", request.getRemoteAddr());
        return ResponseEntity.ok(thanhToanDichVu.xuLyIpn(duLieu));
    }
}