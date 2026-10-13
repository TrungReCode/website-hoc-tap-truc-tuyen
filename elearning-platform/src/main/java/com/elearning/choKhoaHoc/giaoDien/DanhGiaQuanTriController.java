package com.elearning.choKhoaHoc.giaoDien;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhGia;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauXuLyDanhGia;
import com.elearning.choKhoaHoc.ungDung.DanhGiaKhoaHocDichVu;
import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * UC-11 Kiểm duyệt nội dung — nhánh kiểm duyệt đánh giá vi phạm. Chỉ Quản
 * trị viên nội dung hoặc Quản trị viên cấp cao mới được truy cập.
 */
@RestController
@RequestMapping("/api/admin/danh-gia")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('QTV_NOI_DUNG', 'QTV_CAO_CAP')")
public class DanhGiaQuanTriController {

    private final DanhGiaKhoaHocDichVu danhGiaKhoaHocDichVu;

    @GetMapping("/khoa-hoc/{maKhoaHoc}")
    public ResponseEntity<Page<PhanHoiDanhGia>> layTatCaDanhGiaChoAdmin(
            @PathVariable Long maKhoaHoc, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(danhGiaKhoaHocDichVu.layTatCaDanhGiaChoAdmin(maKhoaHoc, pageable));
    }

    @PostMapping("/{maDanhGia}/an")
    public ResponseEntity<PhanHoiDanhGia> anDanhGia(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maDanhGia,
            @Valid @RequestBody YeuCauXuLyDanhGia yeuCau) {
        return ResponseEntity.ok(
                danhGiaKhoaHocDichVu.anDanhGiaViPham(nguoiDung.getMaNguoiDung(), maDanhGia, yeuCau));
    }

    @PostMapping("/{maDanhGia}/bo-an")
    public ResponseEntity<PhanHoiDanhGia> boAnDanhGia(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maDanhGia) {
        return ResponseEntity.ok(danhGiaKhoaHocDichVu.boAnDanhGia(nguoiDung.getMaNguoiDung(), maDanhGia));
    }

    @DeleteMapping("/{maDanhGia}")
    public ResponseEntity<Void> xoaDanhGia(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maDanhGia,
            @Valid @RequestBody YeuCauXuLyDanhGia yeuCau) {
        danhGiaKhoaHocDichVu.xoaDanhGiaViPham(nguoiDung.getMaNguoiDung(), maDanhGia, yeuCau);
        return ResponseEntity.noContent().build();
    }
}
