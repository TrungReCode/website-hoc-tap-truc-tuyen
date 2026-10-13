package com.elearning.lopHocLMS.giaoDien;

import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiDeKiemTra;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiKetQuaQuiz;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauNopBaiKiemTra;
import com.elearning.lopHocLMS.ungDung.KiemTraTrongLopDichVu;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Luồng làm bài kiểm tra khi TỰ HỌC (khóa học cá nhân, không qua lớp học).
 *
 * Trước đây layDeBaiTuHoc()/nopBaiTuHoc() đã có sẵn ở KiemTraTrongLopDichVu
 * nhưng CHƯA từng được expose qua Controller nào — QuizLopController chỉ có
 * route lồng trong /api/lop-hoc/{maLopHoc}/..., không áp dụng được cho học
 * viên tự học (không thuộc lớp nào). Hệ quả: frontend không có API thật để
 * gọi, phải hiển thị tạm đề + đáp án đúng lấy thẳng từ dữ liệu giáo trình
 * (PhanHoiKhoaHoc.giaoTrinh) thay vì tổ chức làm bài — vô tình lộ đáp án
 * đúng ngay khi chưa nộp bài. Controller này bổ sung route còn thiếu.
 */
@RestController
@RequestMapping("/api/khoa-hoc/tu-hoc/quiz")
@RequiredArgsConstructor
public class QuizTuHocController {

    private final KiemTraTrongLopDichVu kiemTraTrongLopDichVu;

    @GetMapping("/{maBaiHoc}/de-bai")
    public ResponseEntity<PhanHoiDeKiemTra> layDeBaiTuHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maBaiHoc) {
        return ResponseEntity.ok(kiemTraTrongLopDichVu.layDeBaiTuHoc(nd.getMaNguoiDung(), maBaiHoc));
    }

    @PostMapping("/{maBaiHoc}/nop-bai")
    public ResponseEntity<PhanHoiKetQuaQuiz> nopBaiTuHoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maBaiHoc,
            @Valid @RequestBody YeuCauNopBaiKiemTra yeuCau) {
        return ResponseEntity.ok(kiemTraTrongLopDichVu.nopBaiTuHoc(nd.getMaNguoiDung(), maBaiHoc, yeuCau));
    }

    @GetMapping("/{maBaiHoc}/ket-qua")
    public ResponseEntity<PhanHoiKetQuaQuiz> layKetQuaGanNhat(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maBaiHoc) {
        return kiemTraTrongLopDichVu.layKetQuaGanNhatGoc(nd.getMaNguoiDung(), maBaiHoc)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
