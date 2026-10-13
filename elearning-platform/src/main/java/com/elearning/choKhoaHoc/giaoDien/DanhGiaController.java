package com.elearning.choKhoaHoc.giaoDien;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhGia;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauDanhGia;
import com.elearning.choKhoaHoc.ungDung.DanhGiaKhoaHocDichVu;
import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * UC-15 Đánh giá khóa học. GET danh sách đánh giá là công khai (không cần
 * JWT) vì Chợ Khóa Học hiển thị đánh giá ngay trên trang chi tiết — đã
 * được thêm vào DUONG_DAN_CONG_KHAI tương ứng tại CauHinhBaoMat.
 */
@RestController
@RequiredArgsConstructor
public class DanhGiaController {

    private final DanhGiaKhoaHocDichVu danhGiaKhoaHocDichVu;

    @GetMapping("/api/khoa-hoc/cong-khai/{maKhoaHoc}/danh-gia")
    public ResponseEntity<Page<PhanHoiDanhGia>> layDanhGiaTheoKhoaHoc(
            @PathVariable Long maKhoaHoc, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(danhGiaKhoaHocDichVu.layDanhGiaTheoKhoaHoc(maKhoaHoc, pageable));
    }

    @GetMapping("/api/khoa-hoc/{maKhoaHoc}/danh-gia/cua-toi")
    public ResponseEntity<PhanHoiDanhGia> layDanhGiaCuaToi(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maKhoaHoc) {
        return danhGiaKhoaHocDichVu.layDanhGiaCuaToi(nguoiDung.getMaNguoiDung(), maKhoaHoc)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/api/khoa-hoc/{maKhoaHoc}/danh-gia")
    public ResponseEntity<PhanHoiDanhGia> taoDanhGia(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maKhoaHoc,
            @Valid @RequestBody YeuCauDanhGia yeuCau) {
        PhanHoiDanhGia ketQua = danhGiaKhoaHocDichVu.taoDanhGia(nguoiDung.getMaNguoiDung(), maKhoaHoc, yeuCau);
        return ResponseEntity.status(201).body(ketQua);
    }

    @PutMapping("/api/danh-gia/{maDanhGia}")
    public ResponseEntity<PhanHoiDanhGia> capNhatDanhGia(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maDanhGia,
            @Valid @RequestBody YeuCauDanhGia yeuCau) {
        return ResponseEntity.ok(danhGiaKhoaHocDichVu.capNhatDanhGia(nguoiDung.getMaNguoiDung(), maDanhGia, yeuCau));
    }

    @DeleteMapping("/api/danh-gia/{maDanhGia}")
    public ResponseEntity<Void> xoaDanhGia(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maDanhGia) {
        danhGiaKhoaHocDichVu.xoaDanhGia(nguoiDung.getMaNguoiDung(), maDanhGia);
        return ResponseEntity.noContent().build();
    }
}
