package com.elearning.traoDoiTrongLop.giaoDien;

import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import com.elearning.dungChung.phanTrang.KetQuaPhanTrang;
import com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi.PhanHoiBaiDang;
import com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi.PhanHoiBinhLuan;
import com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi.PhanHoiThongBao;
import com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi.PhanHoiTrangThaiNopBai;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauCapNhatBaiDang;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauChamDiem;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauNopBaiTap;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauTaoBaiDang;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauTaoBinhLuan;
import com.elearning.traoDoiTrongLop.mienDomain.TepDinhKem;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiBaiDang;
import com.elearning.traoDoiTrongLop.ungDung.BaiDangDichVu;
import com.elearning.traoDoiTrongLop.ungDung.BinhLuanDichVu;
import com.elearning.traoDoiTrongLop.ungDung.ThongBaoDichVu;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller cho module traoDoiTrongLop.
 *
 * Nhóm endpoint:
 *   /api/lop-hoc/{maLopHoc}/bai-dang  — CRUD bài đăng theo lớp
 *   /api/bai-dang/{maBaiDang}/binh-luan — CRUD bình luận theo bài đăng
 *   /api/thong-bao                     — Quản lý thông báo cá nhân
 */
@RestController
@RequiredArgsConstructor
public class TraoDoiTrongLopController {

    private final BaiDangDichVu  baiDangDichVu;
    private final BinhLuanDichVu binhLuanDichVu;
    private final ThongBaoDichVu thongBaoDichVu;

    // ════════════════════════════════════════
    //  BÀI ĐĂNG
    // ════════════════════════════════════════

    @GetMapping("/api/lop-hoc/{maLopHoc}/bai-dang")
    public ResponseEntity<KetQuaPhanTrang<PhanHoiBaiDang>> layDanhSachBaiDang(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maLopHoc,
            @RequestParam(required = false) LoaiBaiDang loai,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(baiDangDichVu.layDanhSach(nd.getMaNguoiDung(), maLopHoc, loai, pageable));
    }

    @GetMapping("/api/bai-dang/{maBaiDang}")
    public ResponseEntity<PhanHoiBaiDang> layChiTietBaiDang(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maBaiDang) {
        return ResponseEntity.ok(baiDangDichVu.layChiTiet(nd.getMaNguoiDung(), maBaiDang));
    }

    @PostMapping(value = "/api/lop-hoc/{maLopHoc}/bai-dang/tai-len-tep", consumes = "multipart/form-data")
    public ResponseEntity<TepDinhKem> taiLenTepDinhKem(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maLopHoc,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(baiDangDichVu.taiLenTepDinhKem(nd.getMaNguoiDung(), maLopHoc, file));
    }

    @PostMapping("/api/lop-hoc/{maLopHoc}/bai-dang")
    public ResponseEntity<PhanHoiBaiDang> taoBaiDang(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maLopHoc,
            @Valid @RequestBody YeuCauTaoBaiDang yeuCau) {
        return ResponseEntity.status(201).body(baiDangDichVu.taoBaiDang(nd.getMaNguoiDung(), maLopHoc, yeuCau));
    }

    @PutMapping("/api/bai-dang/{maBaiDang}")
    public ResponseEntity<PhanHoiBaiDang> capNhatBaiDang(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maBaiDang,
            @Valid @RequestBody YeuCauCapNhatBaiDang yeuCau) {
        return ResponseEntity.ok(baiDangDichVu.capNhatBaiDang(nd.getMaNguoiDung(), maBaiDang, yeuCau));
    }

    @DeleteMapping("/api/bai-dang/{maBaiDang}")
    public ResponseEntity<Void> xoaBaiDang(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maBaiDang) {
        baiDangDichVu.xoaBaiDang(nd.getMaNguoiDung(), maBaiDang);
        return ResponseEntity.noContent().build();
    }

    /** Giảng viên đánh dấu bài HOI_DAP là đã giải đáp. */
    @PatchMapping("/api/bai-dang/{maBaiDang}/giai-dap")
    public ResponseEntity<PhanHoiBaiDang> danhDauGiaiDap(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maBaiDang,
            @RequestParam(defaultValue = "true") boolean giaiDap) {
        return ResponseEntity.ok(baiDangDichVu.danhDauGiaiDap(nd.getMaNguoiDung(), maBaiDang, giaiDap));
    }

    // ════════════════════════════════════════
    //  BÌNH LUẬN
    // ════════════════════════════════════════

    @GetMapping("/api/bai-dang/{maBaiDang}/binh-luan")
    public ResponseEntity<List<PhanHoiBinhLuan>> layBinhLuan(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maBaiDang) {
        return ResponseEntity.ok(binhLuanDichVu.layBinhLuanTheoBAiDang(nd.getMaNguoiDung(), maBaiDang));
    }

    @PostMapping("/api/bai-dang/{maBaiDang}/binh-luan")
    public ResponseEntity<PhanHoiBinhLuan> taoBinhLuan(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maBaiDang,
            @Valid @RequestBody YeuCauTaoBinhLuan yeuCau) {
        return ResponseEntity.status(201)
                .body(binhLuanDichVu.taoBinhLuan(nd.getMaNguoiDung(), maBaiDang, yeuCau));
    }

    @DeleteMapping("/api/binh-luan/{maBinhLuan}")
    public ResponseEntity<Void> xoaBinhLuan(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maBinhLuan) {
        binhLuanDichVu.xoaBinhLuan(nd.getMaNguoiDung(), maBinhLuan);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════
    //  BÀI TẬP — NỘP BÀI / CHẤM ĐIỂM (kiểu Google Classroom)
    // ════════════════════════════════════════

    /** Học viên nộp bài (nộp lại sẽ ghi đè bài nộp trước đó của chính mình). */
    @PostMapping("/api/bai-dang/{maBaiDang}/nop-bai")
    public ResponseEntity<PhanHoiBinhLuan> nopBaiTap(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maBaiDang,
            @Valid @RequestBody YeuCauNopBaiTap yeuCau) {
        return ResponseEntity.status(201).body(binhLuanDichVu.nopBaiTap(nd.getMaNguoiDung(), maBaiDang, yeuCau));
    }

    /** Giảng viên chấm điểm một bài nộp cụ thể (mã bình luận của bài nộp). */
    @PostMapping("/api/binh-luan/{maBinhLuanBaiNop}/cham-diem")
    public ResponseEntity<PhanHoiBinhLuan> chamDiem(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maBinhLuanBaiNop,
            @Valid @RequestBody YeuCauChamDiem yeuCau) {
        return ResponseEntity.ok(binhLuanDichVu.chamDiem(nd.getMaNguoiDung(), maBinhLuanBaiNop, yeuCau));
    }

    /**
     * Giảng viên xem trạng thái nộp bài của toàn bộ học viên trong lớp
     * (đã nộp / chưa nộp / đã chấm) cho một bài đăng loại Bài tập.
     */
    @GetMapping("/api/bai-dang/{maBaiDang}/danh-sach-nop-bai")
    public ResponseEntity<List<PhanHoiTrangThaiNopBai>> layDanhSachNopBai(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maBaiDang) {
        return ResponseEntity.ok(binhLuanDichVu.layDanhSachNopBai(nd.getMaNguoiDung(), maBaiDang));
    }

    // ════════════════════════════════════════
    //  THÔNG BÁO
    // ════════════════════════════════════════

    @GetMapping("/api/thong-bao")
    public ResponseEntity<KetQuaPhanTrang<PhanHoiThongBao>> layThongBao(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(thongBaoDichVu.layThongBaoCuaToi(nd.getMaNguoiDung(), pageable));
    }

    @GetMapping("/api/thong-bao/so-chua-doc")
    public ResponseEntity<Long> demChuaDoc(@AuthenticationPrincipal NguoiDungThamChieu nd) {
        return ResponseEntity.ok(thongBaoDichVu.demChuaDoc(nd.getMaNguoiDung()));
    }

    @PatchMapping("/api/thong-bao/doc-tat-ca")
    public ResponseEntity<Void> docTatCa(@AuthenticationPrincipal NguoiDungThamChieu nd) {
        thongBaoDichVu.danhDauTatCaDaDoc(nd.getMaNguoiDung());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/thong-bao/{maThongBao}/doc")
    public ResponseEntity<Void> docMot(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maThongBao) {
        thongBaoDichVu.danhDauDaDoc(nd.getMaNguoiDung(), maThongBao);
        return ResponseEntity.noContent().build();
    }
}