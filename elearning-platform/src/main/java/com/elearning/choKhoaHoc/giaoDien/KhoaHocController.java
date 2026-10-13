package com.elearning.choKhoaHoc.giaoDien;
 
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhSachKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauCapNhatKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoKhoaHoc;
import com.elearning.choKhoaHoc.haTang.boPhanNgoai.AdapterLuuTruHocLieu;
import com.elearning.choKhoaHoc.ungDung.KhoaHocDichVu;
import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
 
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/khoa-hoc")
@RequiredArgsConstructor
public class KhoaHocController {
 
    private final KhoaHocDichVu khoaHocDichVu;
    private final AdapterLuuTruHocLieu adapterLuuTruHocLieu;
 
    // ---------- Upload ảnh bìa ----------
 
    /**
     * Upload ảnh bìa khóa học lên, trả về URL công khai.
     * Tái dùng AdapterLuuTruHocLieu.taiLenVideo dành cho Cloudinary nhưng
     * với resource_type=image thay vì video — tuy nhiên để đơn giản, ta
     * tải ảnh qua R2 (taiLenTaiLieu) vì ảnh là tệp nhỏ và
     * không cần CDN video. Cloudinary hỗ trợ ảnh nhưng cần thêm overload
     * riêng trong Adapter — thay vào đó dùng R2 cho đồng nhất.
     */
    @PostMapping(value = "/tai-len-anh-bia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> taiLenAnhBia(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
            @RequestParam("file") MultipartFile file) {
 
        if (file == null || file.isEmpty()) {
            throw new NgoaiLeDuLieuKhongHopLe("Vui lòng chọn ảnh bìa");
        }
 
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new NgoaiLeDuLieuKhongHopLe("Chỉ chấp nhận tệp ảnh (JPG, PNG, WebP...)");
        }
 
        var ketQua = adapterLuuTruHocLieu.taiLenTaiLieu(file);
        return ResponseEntity.ok(Map.of("url", ketQua.url()));
    }
 
    // ---------- Chợ Khóa Học — công khai ----------
 
    @GetMapping("/cong-khai")
    public ResponseEntity<Page<PhanHoiDanhSachKhoaHoc>> timKiemCongKhai(
            @RequestParam(required = false) Long maDanhMuc,
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(required = false) String caNhanLoc,
            @RequestParam(required = false) String dayHocLoc,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(khoaHocDichVu.timKiemCongKhai(maDanhMuc, tuKhoa, caNhanLoc, dayHocLoc, pageable));
    }
 
    @GetMapping("/cong-khai/{maKhoaHoc}")
    public ResponseEntity<PhanHoiKhoaHoc> layChiTietCongKhai(@PathVariable Long maKhoaHoc) {
        return ResponseEntity.ok(khoaHocDichVu.layChiTietCongKhai(maKhoaHoc));
    }
 
    // ---------- Quản lý khóa học — Giảng viên ----------
 
    @GetMapping("/cua-toi")
    public ResponseEntity<List<PhanHoiDanhSachKhoaHoc>> layKhoaHocCuaToi(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung) {
        return ResponseEntity.ok(khoaHocDichVu.layKhoaHocCuaToi(nguoiDung.getMaNguoiDung()));
    }

    /** UC-09 Tạo lớp học — khóa học tự viết HOẶC đã mua Giấy phép Giảng dạy. */
    @GetMapping("/co-the-mo-lop")
    public ResponseEntity<List<PhanHoiDanhSachKhoaHoc>> layKhoaHocCoTheMoLop(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung) {
        return ResponseEntity.ok(khoaHocDichVu.layKhoaHocCoTheMoLop(nguoiDung.getMaNguoiDung()));
    }
 
    // ---------- Học viên — khóa học đã mua ----------
 
    @GetMapping("/da-mua")
    public ResponseEntity<List<PhanHoiDanhSachKhoaHoc>> layKhoaHocDaMua(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung) {
        return ResponseEntity.ok(khoaHocDichVu.layKhoaHocDaMua(nguoiDung.getMaNguoiDung()));
    }
 
    @GetMapping("/{maKhoaHoc}/hoc")
    public ResponseEntity<PhanHoiKhoaHoc> layChiTietChoHocVien(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
            @PathVariable Long maKhoaHoc) {
        return ResponseEntity.ok(khoaHocDichVu.layChiTietChoHocVien(nguoiDung.getMaNguoiDung(), maKhoaHoc));
    }
 
    @GetMapping("/{maKhoaHoc}")
    public ResponseEntity<PhanHoiKhoaHoc> layChiTietChoTacGia(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
            @PathVariable Long maKhoaHoc) {
        return ResponseEntity.ok(khoaHocDichVu.layChiTietChoTacGia(nguoiDung.getMaNguoiDung(), maKhoaHoc));
    }
 
    @PostMapping
    public ResponseEntity<PhanHoiKhoaHoc> taoKhoaHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
            @Valid @RequestBody YeuCauTaoKhoaHoc yeuCau) {
        return ResponseEntity.status(201).body(khoaHocDichVu.taoKhoaHoc(nguoiDung.getMaNguoiDung(), yeuCau));
    }
 
    @PutMapping("/{maKhoaHoc}")
    public ResponseEntity<PhanHoiKhoaHoc> capNhatKhoaHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
            @PathVariable Long maKhoaHoc,
            @Valid @RequestBody YeuCauCapNhatKhoaHoc yeuCau) {
        return ResponseEntity.ok(khoaHocDichVu.capNhatKhoaHoc(nguoiDung.getMaNguoiDung(), maKhoaHoc, yeuCau));
    }
 
    @DeleteMapping("/{maKhoaHoc}")
    public ResponseEntity<Void> xoaKhoaHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
            @PathVariable Long maKhoaHoc) {
        khoaHocDichVu.xoaKhoaHoc(nguoiDung.getMaNguoiDung(), maKhoaHoc);
        return ResponseEntity.noContent().build();
    }
 
    @PostMapping("/{maKhoaHoc}/gui-kiem-duyet")
    public ResponseEntity<PhanHoiKhoaHoc> guiKiemDuyet(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
            @PathVariable Long maKhoaHoc) {
        return ResponseEntity.ok(khoaHocDichVu.guiKiemDuyet(nguoiDung.getMaNguoiDung(), maKhoaHoc));
    }
 
    @PostMapping("/{maKhoaHoc}/rut-lai")
    public ResponseEntity<PhanHoiKhoaHoc> rutLaiKiemDuyet(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
            @PathVariable Long maKhoaHoc) {
        return ResponseEntity.ok(khoaHocDichVu.rutLaiKiemDuyet(nguoiDung.getMaNguoiDung(), maKhoaHoc));
    }
 
    @PostMapping("/{maKhoaHoc}/luu-tru")
    public ResponseEntity<PhanHoiKhoaHoc> luuTru(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
            @PathVariable Long maKhoaHoc) {
        return ResponseEntity.ok(khoaHocDichVu.luuTru(nguoiDung.getMaNguoiDung(), maKhoaHoc));
    }
 
    @PostMapping("/{maKhoaHoc}/tai-cong-khai")
    public ResponseEntity<PhanHoiKhoaHoc> taiCongKhai(
            @AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
            @PathVariable Long maKhoaHoc) {
        return ResponseEntity.ok(khoaHocDichVu.taiCongKhai(nguoiDung.getMaNguoiDung(), maKhoaHoc));
    }
}