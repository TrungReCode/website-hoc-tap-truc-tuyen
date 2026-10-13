package com.elearning.choKhoaHoc.giaoDien;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhSachKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauDuyetKhoaHoc;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.TrangThaiKhoaHoc;
import com.elearning.choKhoaHoc.ungDung.DuyetKhoaHocDichVu;
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
 * UC-11 Kiểm duyệt nội dung. Chỉ Quản trị viên nội dung hoặc Quản trị
 * viên cấp cao mới được truy cập toàn bộ Controller này — NguoiDung.
 * getAuthorities() đã trả về "ROLE_" + tên vai trò nên hasAnyRole() ở
 * đây so khớp trực tiếp với enum VaiTro mà không cần parse thủ công.
 */
@RestController
@RequestMapping("/api/khoa-hoc/cho-duyet")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('QTV_NOI_DUNG', 'QTV_CAO_CAP')")
public class DuyetKhoaHocController {

    private final DuyetKhoaHocDichVu duyetKhoaHocDichVu;

    @GetMapping
    public ResponseEntity<Page<PhanHoiDanhSachKhoaHoc>> layDanhSachQuanLy(
            @RequestParam(required = false) TrangThaiKhoaHoc trangThai,
            @RequestParam(required = false) String tuKhoa,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(duyetKhoaHocDichVu.layDanhSachQuanLy(trangThai, tuKhoa, pageable));
    }

    @GetMapping("/{maKhoaHoc}")
    public ResponseEntity<PhanHoiKhoaHoc> moHoSoKiemDuyet(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maKhoaHoc) {
        return ResponseEntity.ok(duyetKhoaHocDichVu.moHoSoKiemDuyet(nguoiDung.getMaNguoiDung(), maKhoaHoc));
    }

    @PostMapping("/{maKhoaHoc}/dong")
    public ResponseEntity<Void> dongHoSoKiemDuyet(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maKhoaHoc) {
        duyetKhoaHocDichVu.dongHoSoKiemDuyet(nguoiDung.getMaNguoiDung(), maKhoaHoc);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{maKhoaHoc}/quyet-dinh")
    public ResponseEntity<PhanHoiKhoaHoc> duyetKhoaHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung, @PathVariable Long maKhoaHoc,
            @Valid @RequestBody YeuCauDuyetKhoaHoc yeuCau) {
        return ResponseEntity.ok(duyetKhoaHocDichVu.duyetKhoaHoc(nguoiDung.getMaNguoiDung(), maKhoaHoc, yeuCau));
    }
}