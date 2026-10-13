package com.elearning.lopHocLMS.giaoDien;

import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiDanhGiaLop;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCapNhatLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauDanhGiaLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauTaoLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCapNhatTienDo;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;
import com.elearning.lopHocLMS.ungDung.CauHinhLopDichVu;
import com.elearning.lopHocLMS.ungDung.DanhGiaLopHocDichVu;
import com.elearning.lopHocLMS.ungDung.GhiDanhDichVu;
import com.elearning.lopHocLMS.ungDung.TaoLopHocDichVu;
import com.elearning.lopHocLMS.ungDung.TienDoHocDichVu;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lop-hoc")
@RequiredArgsConstructor
public class LopHocController {

    private final TaoLopHocDichVu taoLopHocDichVu;
    private final CauHinhLopDichVu cauHinhLopDichVu;
    private final GhiDanhDichVu ghiDanhDichVu;
    private final DanhGiaLopHocDichVu danhGiaLopHocDichVu;
    private final TienDoHocDichVu tienDoHocDichVu;

    // ---------- Công khai — không cần đăng nhập ----------

    /**
     * Danh sách lớp học đang hoạt động (congKhai=true, chưa đóng) — trang
     * "Danh sách lớp học" tương tự Chợ Khóa Học. Đăng ký trong
     * CauHinhBaoMat.DUONG_DAN_CONG_KHAI để không yêu cầu JWT.
     */
    @GetMapping("/cong-khai")
    public ResponseEntity<Page<PhanHoiLop>> layDanhSachCongKhai(
            @RequestParam(required = false) Long maKhoaHoc,
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(required = false) String gia,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(taoLopHocDichVu.layDanhSachCongKhai(maKhoaHoc, tuKhoa, gia, pageable));
    }

    // ---------- Giảng viên ----------

    @GetMapping("/cua-toi")
    public ResponseEntity<List<PhanHoiLop>> layLopGiangVien(@AuthenticationPrincipal NguoiDungThamChieu nd) {
        return ResponseEntity.ok(taoLopHocDichVu.layLopCuaGiangVien(nd.getMaNguoiDung()));
    }

    /**
     * Dành cho Tác giả khóa học: xem toàn bộ lớp học (của mọi giảng viên
     * đang giữ Giấy phép Giảng dạy, không chỉ của riêng tác giả) đang được
     * xây dựng dựa trên một khóa học cụ thể mà mình là tác giả.
     */
    @GetMapping("/theo-khoa-hoc/{maKhoaHoc}")
    public ResponseEntity<List<PhanHoiLop>> layLopTheoKhoaHoc(@AuthenticationPrincipal NguoiDungThamChieu nd,
                                                                @PathVariable Long maKhoaHoc) {
        return ResponseEntity.ok(taoLopHocDichVu.layLopTheoKhoaHocChoTacGia(nd.getMaNguoiDung(), maKhoaHoc));
    }

    @PostMapping
    public ResponseEntity<PhanHoiLop> taoLop(@AuthenticationPrincipal NguoiDungThamChieu nd,
                                               @Valid @RequestBody YeuCauTaoLop yeuCau) {
        return ResponseEntity.status(201).body(taoLopHocDichVu.taoLop(nd.getMaNguoiDung(), yeuCau));
    }

    @GetMapping("/{maLopHoc}/quan-ly")
    public ResponseEntity<PhanHoiLop> layChiTietGiangVien(@AuthenticationPrincipal NguoiDungThamChieu nd,
                                                            @PathVariable Long maLopHoc) {
        return ResponseEntity.ok(taoLopHocDichVu.layChiTietChoGiangVien(nd.getMaNguoiDung(), maLopHoc));
    }

    @PutMapping("/{maLopHoc}/cau-hinh")
    public ResponseEntity<PhanHoiLop> capNhatCauHinh(@AuthenticationPrincipal NguoiDungThamChieu nd,
                                                       @PathVariable Long maLopHoc,
                                                       @Valid @RequestBody YeuCauCapNhatLop yeuCau) {
        return ResponseEntity.ok(cauHinhLopDichVu.capNhatCauHinh(nd.getMaNguoiDung(), maLopHoc, yeuCau));
    }

    @PostMapping("/{maLopHoc}/dong")
    public ResponseEntity<PhanHoiLop> dongLop(@AuthenticationPrincipal NguoiDungThamChieu nd,
                                               @PathVariable Long maLopHoc) {
        return ResponseEntity.ok(cauHinhLopDichVu.dongLop(nd.getMaNguoiDung(), maLopHoc));
    }

    @DeleteMapping("/{maLopHoc}")
    public ResponseEntity<Void> xoaLop(@AuthenticationPrincipal NguoiDungThamChieu nd,
                                        @PathVariable Long maLopHoc) {
        taoLopHocDichVu.xoaLop(nd.getMaNguoiDung(), maLopHoc);
        return ResponseEntity.noContent().build();
    }

    // ---------- Quản trị viên nội dung ----------

    @GetMapping("/quan-tri")
    @PreAuthorize("hasAnyRole('QTV_NOI_DUNG', 'QTV_CAO_CAP')")
    public ResponseEntity<Page<PhanHoiLop>> layDanhSachChoAdmin(
            @RequestParam(required = false) TrangThaiLop trangThai,
            @RequestParam(required = false) String maLop,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(taoLopHocDichVu.layDanhSachChoAdmin(trangThai, maLop, pageable));
    }

    // ---------- Học viên ----------

    @GetMapping("/cua-hoc-vien")
    public ResponseEntity<List<PhanHoiLop>> layLopHocVien(@AuthenticationPrincipal NguoiDungThamChieu nd) {
        return ResponseEntity.ok(ghiDanhDichVu.layLopCuaHocVien(nd.getMaNguoiDung()));
    }

    // ---------- Đánh giá lớp ----------

    @GetMapping("/{maLopHoc}/danh-gia")
    public ResponseEntity<Page<PhanHoiDanhGiaLop>> layDanhGia(@PathVariable Long maLopHoc,
                                                                @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(danhGiaLopHocDichVu.layDanhGiaTheoLop(maLopHoc, pageable));
    }

    @GetMapping("/{maLopHoc}/danh-gia/cua-toi")
    public ResponseEntity<PhanHoiDanhGiaLop> layDanhGiaCuaToi(@AuthenticationPrincipal NguoiDungThamChieu nd,
                                                                @PathVariable Long maLopHoc) {
        return danhGiaLopHocDichVu.layDanhGiaCuaToi(nd.getMaNguoiDung(), maLopHoc)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/{maLopHoc}/danh-gia")
    public ResponseEntity<PhanHoiDanhGiaLop> taoDanhGia(@AuthenticationPrincipal NguoiDungThamChieu nd,
                                                          @PathVariable Long maLopHoc,
                                                          @Valid @RequestBody YeuCauDanhGiaLop yeuCau) {
        return ResponseEntity.status(201).body(danhGiaLopHocDichVu.taoDanhGia(nd.getMaNguoiDung(), maLopHoc, yeuCau));
    }

    @PutMapping("/danh-gia/{maDanhGia}")
    public ResponseEntity<PhanHoiDanhGiaLop> capNhatDanhGia(@AuthenticationPrincipal NguoiDungThamChieu nd,
                                                              @PathVariable Long maDanhGia,
                                                              @Valid @RequestBody YeuCauDanhGiaLop yeuCau) {
        return ResponseEntity.ok(danhGiaLopHocDichVu.capNhatDanhGia(nd.getMaNguoiDung(), maDanhGia, yeuCau));
    }

    @DeleteMapping("/danh-gia/{maDanhGia}")
    public ResponseEntity<Void> xoaDanhGia(@AuthenticationPrincipal NguoiDungThamChieu nd,
                                            @PathVariable Long maDanhGia) {
        danhGiaLopHocDichVu.xoaDanhGia(nd.getMaNguoiDung(), maDanhGia);
        return ResponseEntity.noContent().build();
    }

    // ---------- Tiến độ học tập ----------

    @PutMapping("/{maLopHoc}/tien-do/goc/{maBaiHoc}")
    public ResponseEntity<Void> capNhatTienDoGoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maLopHoc, 
            @PathVariable Long maBaiHoc, 
            @RequestBody YeuCauCapNhatTienDo yeuCau) {
            
        tienDoHocDichVu.capNhatTienDoBaiGoc(nd.getMaNguoiDung(), maLopHoc, maBaiHoc, yeuCau);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{maLopHoc}/tien-do/bo-sung/{maBaiHocBoSung}")
    public ResponseEntity<Void> capNhatTienDoBoSung(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maLopHoc, 
            @PathVariable Long maBaiHocBoSung, 
            @RequestBody YeuCauCapNhatTienDo yeuCau) {
            
        tienDoHocDichVu.capNhatTienDoBaiBoSung(nd.getMaNguoiDung(), maBaiHocBoSung, yeuCau);
        return ResponseEntity.ok().build();
    }
}