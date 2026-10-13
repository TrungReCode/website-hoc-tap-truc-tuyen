package com.elearning.dinhDanhPhanQuyen.giaoDien;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiXacThucGiangVien;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDuyetXacThucGiangVien;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauXacThucGiangVien;
import com.elearning.dinhDanhPhanQuyen.ungDung.XacThucGiangVienDichVu;
import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class XacThucGiangVienController {

    private final XacThucGiangVienDichVu xacThucGiangVienDichVu;

    @PostMapping(value = "/api/xac-thuc-giang-vien", consumes = "multipart/form-data")
    public ResponseEntity<PhanHoiXacThucGiangVien> guiYeuCau(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @RequestParam LocalDate ngaySinh,
            @RequestParam String bangCap,
            @RequestParam("file") MultipartFile fileMinhChung) {
        YeuCauXacThucGiangVien yeuCau = new YeuCauXacThucGiangVien(ngaySinh, bangCap);
        return ResponseEntity.status(201)
                .body(xacThucGiangVienDichVu.guiYeuCau(nd.getMaNguoiDung(), yeuCau, fileMinhChung));
    }

    @GetMapping("/api/xac-thuc-giang-vien/cua-toi")
    public ResponseEntity<PhanHoiXacThucGiangVien> layTrangThaiCuaToi(@AuthenticationPrincipal NguoiDungThamChieu nd) {
        return xacThucGiangVienDichVu.layTrangThaiCuaToi(nd.getMaNguoiDung())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/api/admin/xac-thuc-giang-vien")
    @PreAuthorize("hasAnyRole('QTV_NOI_DUNG', 'QTV_CAO_CAP')")
    public ResponseEntity<Page<PhanHoiXacThucGiangVien>> layDanhSachChoAdmin(
            @RequestParam(required = false) String trangThai, Pageable pageable) {
        return ResponseEntity.ok(xacThucGiangVienDichVu.layDanhSachChoAdmin(trangThai, pageable));
    }

    @PostMapping("/api/admin/xac-thuc-giang-vien/{maXacThuc}/duyet")
    @PreAuthorize("hasAnyRole('QTV_NOI_DUNG', 'QTV_CAO_CAP')")
    public ResponseEntity<PhanHoiXacThucGiangVien> duyetYeuCau(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maXacThuc,
            @RequestBody YeuCauDuyetXacThucGiangVien yeuCau) {
        return ResponseEntity.ok(xacThucGiangVienDichVu.duyetYeuCau(nd.getMaNguoiDung(), maXacThuc, yeuCau));
    }

    @GetMapping("/api/admin/xac-thuc-giang-vien/{maXacThuc}/file")
    @PreAuthorize("hasAnyRole('QTV_NOI_DUNG', 'QTV_CAO_CAP')")
    public ResponseEntity<byte[]> taiFileMinhChung(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maXacThuc) {
        var ketQua = xacThucGiangVienDichVu.taiFileMinhChung(nd.getMaNguoiDung(), maXacThuc);
        String tenFile = ketQua.tenFileGoc() != null ? ketQua.tenFileGoc() : "minh-chung";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + tenFile + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(ketQua.noiDung());
    }
}
