package com.elearning.lopHocLMS.giaoDien;

import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiCauHoiBoSung;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiDeKiemTraChoAdmin;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiMucGiaoTrinhLai;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.*;
import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiHocLieuBoSung;
import com.elearning.lopHocLMS.ungDung.BaiBoSungDichVu;
import com.elearning.lopHocLMS.ungDung.GiaoTrinhLaiDichVu;
import com.elearning.lopHocLMS.ungDung.KiemTraTrongLopDichVu;
import com.elearning.lopHocLMS.ungDung.TienDoHocDichVu;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GiaoTrinhLaiController {

    private final GiaoTrinhLaiDichVu giaoTrinhLaiDichVu;
    private final BaiBoSungDichVu baiBoSungDichVu;
    private final TienDoHocDichVu tienDoHocDichVu;
    private final KiemTraTrongLopDichVu kiemTraTrongLopDichVu;

    @GetMapping("/api/lop-hoc/{maLopHoc}/giao-trinh/quan-ly")
    public ResponseEntity<List<PhanHoiMucGiaoTrinhLai>> layGiaoTrinhGiangVien(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maLopHoc) {
        return ResponseEntity.ok(giaoTrinhLaiDichVu.layGiaoTrinhLaiChoGiangVien(nd.getMaNguoiDung(), maLopHoc));
    }

    /**
     * Cho Quản trị viên nội dung giám sát nội dung lớp học — bao gồm cả bài
     * học bổ sung do giảng viên thêm trực tiếp, vốn không đi qua luồng kiểm
     * duyệt khóa học (xem GiaoTrinhLaiDichVu.layGiaoTrinhLaiChoAdmin).
     */
    @GetMapping("/api/admin/lop-hoc/{maLopHoc}/giao-trinh")
    @PreAuthorize("hasAnyRole('QTV_NOI_DUNG', 'QTV_CAO_CAP')")
    public ResponseEntity<List<PhanHoiMucGiaoTrinhLai>> layGiaoTrinhChoAdmin(@PathVariable Long maLopHoc) {
        return ResponseEntity.ok(giaoTrinhLaiDichVu.layGiaoTrinhLaiChoAdmin(maLopHoc));
    }

    /**
     * Xem chi tiết câu hỏi + đáp án đúng của 1 bài Quiz trong giáo trình lớp
     * (loaiNguon lấy từ PhanHoiMucGiaoTrinhLai.loaiNguon, maThamChieu lấy từ
     * PhanHoiMucGiaoTrinhLai.maThamChieu) — dùng khi admin cần kiểm tra sâu
     * nội dung câu hỏi/đáp án của 1 mục Quiz cụ thể sau khi đã thấy trong
     * danh sách giáo trình ở trên.
     */
    @GetMapping("/api/admin/quiz/{loaiNguon}/{maThamChieu}")
    @PreAuthorize("hasAnyRole('QTV_NOI_DUNG', 'QTV_CAO_CAP')")
    public ResponseEntity<PhanHoiDeKiemTraChoAdmin> layDeBaiQuizChoAdmin(
            @PathVariable String loaiNguon, @PathVariable Long maThamChieu) {
        return ResponseEntity.ok(kiemTraTrongLopDichVu.layDeBaiChoAdmin(loaiNguon, maThamChieu));
    }

    @GetMapping("/api/lop-hoc/{maLopHoc}/giao-trinh")
    public ResponseEntity<List<PhanHoiMucGiaoTrinhLai>> layGiaoTrinhHocVien(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maLopHoc) {
        return ResponseEntity.ok(giaoTrinhLaiDichVu.layGiaoTrinhLaiChoHocVien(nd.getMaNguoiDung(), maLopHoc));
    }

    @PutMapping("/api/lop-hoc/{maLopHoc}/cau-hinh-bai-goc/{maBaiHoc}")
    public ResponseEntity<Void> capNhatCauHinhBaiGoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maLopHoc,
            @PathVariable Long maBaiHoc, @RequestBody YeuCauCauHinhBaiGoc yeuCau) {
        giaoTrinhLaiDichVu.capNhatCauHinhBaiGoc(nd.getMaNguoiDung(), maLopHoc, maBaiHoc, yeuCau);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/api/lop-hoc/{maLopHoc}/bai-bo-sung", consumes = "multipart/form-data")
    public ResponseEntity<PhanHoiMucGiaoTrinhLai> themBaiBoSung(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maLopHoc,
            @RequestParam String tieuDe, @RequestParam(required = false) String moTa,
            @RequestParam LoaiHocLieuBoSung loai,
            @RequestParam(defaultValue = "true") boolean anHien,
            @RequestParam(required = false) Float viTriSau,
            @RequestParam(required = false) Float viTriTruoc,
            @RequestParam(required = false) String duongDanLink,
            @RequestParam(required = false) Integer diemToiThieu,
            @RequestParam(required = false) Integer thoiGian,
            @RequestParam(required = false) MultipartFile file) {
        YeuCauTaoBaiBoSung yeuCau = new YeuCauTaoBaiBoSung(tieuDe, moTa, loai, anHien,
                viTriSau, viTriTruoc, duongDanLink, diemToiThieu, thoiGian);
        return ResponseEntity.status(201).body(baiBoSungDichVu.themBaiBoSung(nd.getMaNguoiDung(), maLopHoc, yeuCau, file));
    }

    @PutMapping("/api/bai-bo-sung/{maBaiHocBoSung}")
    public ResponseEntity<PhanHoiMucGiaoTrinhLai> capNhatBaiBoSung(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maBaiHocBoSung,
            @Valid @RequestBody YeuCauCapNhatBaiBoSung yeuCau) {
        return ResponseEntity.ok(baiBoSungDichVu.capNhatBaiBoSung(nd.getMaNguoiDung(), maBaiHocBoSung, yeuCau));
    }

    @DeleteMapping("/api/bai-bo-sung/{maBaiHocBoSung}")
    public ResponseEntity<Void> xoaBaiBoSung(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maBaiHocBoSung) {
        baiBoSungDichVu.xoaBaiBoSung(nd.getMaNguoiDung(), maBaiHocBoSung);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/bai-bo-sung/{maBaiHocBoSung}/cau-hoi")
    public ResponseEntity<Void> themCauHoiBoSung(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maBaiHocBoSung,
            @Valid @RequestBody YeuCauTaoCauHoiBoSung yeuCau) {
        baiBoSungDichVu.themCauHoiBoSung(nd.getMaNguoiDung(), maBaiHocBoSung, yeuCau);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/api/bai-bo-sung/{maBaiHocBoSung}/cau-hoi")
    public ResponseEntity<List<PhanHoiCauHoiBoSung>> layCauHoiBoSung(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maBaiHocBoSung) {
        return ResponseEntity.ok(baiBoSungDichVu.layCauHoiBoSung(nd.getMaNguoiDung(), maBaiHocBoSung));
    }

    @DeleteMapping("/api/cau-hoi-bo-sung/{maCauHoiBoSung}")
    public ResponseEntity<Void> xoaCauHoiBoSung(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maCauHoiBoSung) {
        baiBoSungDichVu.xoaCauHoiBoSung(nd.getMaNguoiDung(), maCauHoiBoSung);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/lop-hoc/{maLopHoc}/tien-do/bai-goc/{maBaiHoc}")
    public ResponseEntity<Void> capNhatTienDoBaiGoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maLopHoc,
            @PathVariable Long maBaiHoc, @RequestBody YeuCauCapNhatTienDo yeuCau) {
        tienDoHocDichVu.capNhatTienDoBaiGoc(nd.getMaNguoiDung(), maLopHoc, maBaiHoc, yeuCau);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/tien-do/bai-bo-sung/{maBaiHocBoSung}")
    public ResponseEntity<Void> capNhatTienDoBaiBoSung(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maBaiHocBoSung,
            @RequestBody YeuCauCapNhatTienDo yeuCau) {
        tienDoHocDichVu.capNhatTienDoBaiBoSung(nd.getMaNguoiDung(), maBaiHocBoSung, yeuCau);
        return ResponseEntity.noContent().build();
    }

    /**
     * Đánh dấu tiến độ TỰ HỌC (không qua lớp) — dùng khi học viên chỉ mua
     * Giấy phép cá nhân, xem trực tiếp bài học gốc của khóa học mà không
     * tham gia lớp nào.
     */
    @PutMapping("/api/tien-do/tu-hoc/bai-hoc/{maBaiHoc}")
    public ResponseEntity<Void> capNhatTienDoCaNhan(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maBaiHoc,
            @RequestBody YeuCauCapNhatTienDo yeuCau) {
        tienDoHocDichVu.capNhatTienDoCaNhan(nd.getMaNguoiDung(), maBaiHoc, yeuCau);
        return ResponseEntity.noContent().build();
    }
}