package com.elearning.choKhoaHoc.giaoDien;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiBaiHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiChuong;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauCapNhatBaiHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoBaiHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoCauHoi;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoChuong;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.LoaiBaiHoc;
import com.elearning.choKhoaHoc.ungDung.QuanLyNoiDungDichVu;
import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * UC-08 (phần soạn giáo trình). Endpoint thêm bài học là multipart/form-data
 * (kèm file video/tài liệu) nên không dùng @RequestBody JSON như các
 * Controller khác trong hệ thống.
 */
@RestController
@RequiredArgsConstructor
public class NoiDungKhoaHocController {

    private final QuanLyNoiDungDichVu quanLyNoiDungDichVu;

    @PostMapping("/api/khoa-hoc/{maKhoaHoc}/chuong")
    public ResponseEntity<PhanHoiChuong> themChuong(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maKhoaHoc,
            @Valid @RequestBody YeuCauTaoChuong yeuCau) {
        PhanHoiChuong ketQua = quanLyNoiDungDichVu.themChuong(nguoiDung.getMaNguoiDung(), maKhoaHoc, yeuCau);
        return ResponseEntity.status(201).body(ketQua);
    }

    @PutMapping("/api/chuong/{maChuong}")
    public ResponseEntity<PhanHoiChuong> capNhatChuong(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maChuong,
            @Valid @RequestBody YeuCauTaoChuong yeuCau) {
        return ResponseEntity.ok(quanLyNoiDungDichVu.capNhatChuong(nguoiDung.getMaNguoiDung(), maChuong, yeuCau));
    }

    @DeleteMapping("/api/chuong/{maChuong}")
    public ResponseEntity<Void> xoaChuong(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maChuong) {
        quanLyNoiDungDichVu.xoaChuong(nguoiDung.getMaNguoiDung(), maChuong);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/api/chuong/{maChuong}/bai-hoc", consumes = "multipart/form-data")
    public ResponseEntity<PhanHoiBaiHoc> themBaiHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maChuong,
            @RequestParam String tieuDe,
            @RequestParam LoaiBaiHoc loai,
            @RequestParam Integer viTri,
            @RequestParam(defaultValue = "false") boolean xemThu,
            @RequestParam(required = false) Integer diemToiThieu,
            @RequestParam(required = false) Integer thoiGian,
            @RequestParam(required = false) MultipartFile file) {
        YeuCauTaoBaiHoc yeuCau = new YeuCauTaoBaiHoc(tieuDe, loai, viTri, xemThu, diemToiThieu, thoiGian);
        PhanHoiBaiHoc ketQua = quanLyNoiDungDichVu.themBaiHoc(nguoiDung.getMaNguoiDung(), maChuong, yeuCau, file);
        return ResponseEntity.status(201).body(ketQua);
    }

    @PutMapping("/api/bai-hoc/{maBaiHoc}")
    public ResponseEntity<PhanHoiBaiHoc> capNhatBaiHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maBaiHoc,
            @Valid @RequestBody YeuCauCapNhatBaiHoc yeuCau) {
        return ResponseEntity.ok(quanLyNoiDungDichVu.capNhatBaiHoc(nguoiDung.getMaNguoiDung(), maBaiHoc, yeuCau));
    }

    @DeleteMapping("/api/bai-hoc/{maBaiHoc}")
    public ResponseEntity<Void> xoaBaiHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maBaiHoc) {
        quanLyNoiDungDichVu.xoaBaiHoc(nguoiDung.getMaNguoiDung(), maBaiHoc);
        return ResponseEntity.noContent().build();
    }

    /**
     * Thay thế cho xóa vật lý khi khóa học đã có học viên sở hữu (xem
     * QuanLyNoiDungDichVuImpl.xoaBaiHoc) — ẩn tạm thời bài học khỏi mọi
     * luồng hiển thị cho học viên/khách xem mà không mất dữ liệu.
     */
    @PatchMapping("/api/bai-hoc/{maBaiHoc}/an")
    public ResponseEntity<PhanHoiBaiHoc> anBaiHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maBaiHoc) {
        return ResponseEntity.ok(quanLyNoiDungDichVu.anBaiHoc(nguoiDung.getMaNguoiDung(), maBaiHoc));
    }

    @PatchMapping("/api/bai-hoc/{maBaiHoc}/hien")
    public ResponseEntity<PhanHoiBaiHoc> hienBaiHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maBaiHoc) {
        return ResponseEntity.ok(quanLyNoiDungDichVu.hienBaiHoc(nguoiDung.getMaNguoiDung(), maBaiHoc));
    }

    @PostMapping("/api/bai-hoc/{maBaiHoc}/cau-hoi")
    public ResponseEntity<PhanHoiBaiHoc> themCauHoi(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maBaiHoc,
            @Valid @RequestBody YeuCauTaoCauHoi yeuCau) {
        PhanHoiBaiHoc ketQua = quanLyNoiDungDichVu.themCauHoi(nguoiDung.getMaNguoiDung(), maBaiHoc, yeuCau);
        return ResponseEntity.status(201).body(ketQua);
    }

    @DeleteMapping("/api/cau-hoi/{maCauHoi}")
    public ResponseEntity<Void> xoaCauHoi(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maCauHoi) {
        quanLyNoiDungDichVu.xoaCauHoi(nguoiDung.getMaNguoiDung(), maCauHoi);
        return ResponseEntity.noContent().build();
    }
}
