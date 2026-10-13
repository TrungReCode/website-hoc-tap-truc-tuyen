package com.elearning.chungChi.giaoDien;

import com.elearning.chungChi.giaoDien.dto.phanHoi.PhanHoiGiayXacNhan;
import com.elearning.chungChi.giaoDien.dto.phanHoi.PhanHoiTienDo;
import com.elearning.chungChi.ungDung.GiayXacNhanDichVu;
import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChungChiController {

    private final GiayXacNhanDichVu giayXacNhanDichVu;

    // ── Tiến độ ──────────────────────────────────────────────────────────

    @GetMapping("/api/chung-chi/tien-do/ca-nhan/{maKhoaHoc}")
    public ResponseEntity<PhanHoiTienDo> xemTienDoCaNhan(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maKhoaHoc) {
        return ResponseEntity.ok(giayXacNhanDichVu.tinhLaiTienDoCaNhan(nd.getMaNguoiDung(), maKhoaHoc));
    }

    @GetMapping("/api/chung-chi/tien-do/lop-hoc/{maLopHoc}")
    public ResponseEntity<PhanHoiTienDo> xemTienDoLop(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maLopHoc) {
        return ResponseEntity.ok(giayXacNhanDichVu.tinhLaiTienDoLop(nd.getMaNguoiDung(), maLopHoc));
    }

    // ── Cấp giấy (idempotent — gọi lại không cấp trùng) ────────────────

    @PostMapping("/api/chung-chi/cap/ca-nhan/{maKhoaHoc}")
    public ResponseEntity<PhanHoiGiayXacNhan> capGiayCaNhan(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maKhoaHoc) {
        return ResponseEntity.ok(giayXacNhanDichVu.capGiayCaNhan(nd.getMaNguoiDung(), maKhoaHoc));
    }

    @PostMapping("/api/chung-chi/cap/lop-hoc/{maLopHoc}")
    public ResponseEntity<PhanHoiGiayXacNhan> capGiayLopHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maLopHoc) {
        return ResponseEntity.ok(giayXacNhanDichVu.capGiayLopHoc(nd.getMaNguoiDung(), maLopHoc));
    }

    // ── Danh sách giấy của tôi ──────────────────────────────────────────

    @GetMapping("/api/chung-chi/cua-toi")
    public ResponseEntity<List<PhanHoiGiayXacNhan>> layGiayCuaToi(
            @AuthenticationPrincipal NguoiDungThamChieu nd) {
        return ResponseEntity.ok(giayXacNhanDichVu.layDanhSachGiayCuaHocVien(nd.getMaNguoiDung()));
    }

    // ── Xác thực công khai — KHÔNG yêu cầu đăng nhập ───────────────────
    // Đăng ký trong CauHinhBaoMat.DUONG_DAN_CONG_KHAI: "/api/chung-chi/xac-thuc/**"

    @GetMapping("/api/chung-chi/xac-thuc/{maXacThuc}")
    public ResponseEntity<PhanHoiGiayXacNhan> xacThuc(@PathVariable String maXacThuc) {
        return giayXacNhanDichVu.xacThuc(maXacThuc)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("giấy xác nhận", maXacThuc));
    }

    /** Redirect thẳng sang URL công khai R2 — không proxy file qua backend. */
    @GetMapping("/api/chung-chi/xac-thuc/{maXacThuc}/pdf")
    public ResponseEntity<Void> taiPdf(@PathVariable String maXacThuc) {
        String url = giayXacNhanDichVu.layUrlFilePdf(maXacThuc);
        return ResponseEntity.status(302).location(URI.create(url)).build();
    }
}