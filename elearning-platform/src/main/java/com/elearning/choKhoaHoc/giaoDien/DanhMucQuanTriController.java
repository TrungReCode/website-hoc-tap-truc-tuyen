package com.elearning.choKhoaHoc.giaoDien;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhMucQuanTri;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauCapNhatDanhMuc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoDanhMuc;
import com.elearning.choKhoaHoc.ungDung.DanhMucDichVu;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Quản lý danh mục khóa học — dành cho Quản trị viên nội dung/cấp cao.
 *
 * LƯU Ý ĐƯỜNG DẪN: cố tình đặt "/api/danh-muc-quan-tri" thay vì
 * "/api/danh-muc/quan-tri" — vì CauHinhBaoMat khai báo "/api/danh-muc/**"
 * là permitAll (cho Chợ Khóa Học công khai đọc danh sách). Nếu đặt dưới
 * đúng tiền tố đó, request vẫn bị chặn đúng nhờ @PreAuthorize ở tầng
 * method-security, nhưng để tránh nhầm lẫn/rủi ro cấu hình sau này, ta
 * dùng hẳn một tiền tố riêng nằm ngoài pattern permitAll, mặc định rơi
 * vào .anyRequest().authenticated() + @PreAuthorize bên dưới.
 */
@RestController
@RequestMapping("/api/danh-muc-quan-tri")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('QTV_NOI_DUNG', 'QTV_CAO_CAP')")
public class DanhMucQuanTriController {

    private final DanhMucDichVu danhMucDichVu;

    @GetMapping
    public ResponseEntity<List<PhanHoiDanhMucQuanTri>> layTatCa() {
        return ResponseEntity.ok(danhMucDichVu.layTatCaChoQuanTri());
    }

    @PostMapping
    public ResponseEntity<PhanHoiDanhMucQuanTri> taoDanhMuc(@Valid @RequestBody YeuCauTaoDanhMuc yeuCau) {
        return ResponseEntity.status(201).body(danhMucDichVu.taoDanhMuc(yeuCau));
    }

    @PutMapping("/{maDanhMuc}")
    public ResponseEntity<PhanHoiDanhMucQuanTri> capNhatDanhMuc(
            @PathVariable Long maDanhMuc, @Valid @RequestBody YeuCauCapNhatDanhMuc yeuCau) {
        return ResponseEntity.ok(danhMucDichVu.capNhatDanhMuc(maDanhMuc, yeuCau));
    }

    @DeleteMapping("/{maDanhMuc}")
    public ResponseEntity<Void> xoaDanhMuc(@PathVariable Long maDanhMuc) {
        danhMucDichVu.xoaDanhMuc(maDanhMuc);
        return ResponseEntity.noContent().build();
    }
}