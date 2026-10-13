package com.elearning.choKhoaHoc.giaoDien;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhMuc;
import com.elearning.choKhoaHoc.ungDung.DanhMucDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Khớp /api/danh-muc/** đã khai báo công khai trong CauHinhBaoMat.
 */
@RestController
@RequestMapping("/api/danh-muc")
@RequiredArgsConstructor
public class DanhMucController {

    private final DanhMucDichVu danhMucDichVu;

    @GetMapping
    public ResponseEntity<List<PhanHoiDanhMuc>> layTatCa() {
        return ResponseEntity.ok(danhMucDichVu.layTatCa());
    }
}
