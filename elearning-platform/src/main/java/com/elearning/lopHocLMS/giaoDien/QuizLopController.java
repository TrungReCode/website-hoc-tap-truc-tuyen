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

@RestController
@RequestMapping("/api/lop-hoc/{maLopHoc}")
@RequiredArgsConstructor
public class QuizLopController {

    private final KiemTraTrongLopDichVu kiemTraTrongLopDichVu;

    @GetMapping("/quiz/goc/{maBaiHoc}/de-bai")
    public ResponseEntity<PhanHoiDeKiemTra> layDeBaiGoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maLopHoc, @PathVariable Long maBaiHoc) {
        return ResponseEntity.ok(kiemTraTrongLopDichVu.layDeBaiGoc(nd.getMaNguoiDung(), maLopHoc, maBaiHoc));
    }

    @GetMapping("/quiz/bo-sung/{maBaiHocBoSung}/de-bai")
    public ResponseEntity<PhanHoiDeKiemTra> layDeBoSung(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maLopHoc, @PathVariable Long maBaiHocBoSung) {
        return ResponseEntity.ok(kiemTraTrongLopDichVu.layDeBoSung(nd.getMaNguoiDung(), maLopHoc, maBaiHocBoSung));
    }

    @PostMapping("/quiz/goc/{maBaiHoc}/nop-bai")
    public ResponseEntity<PhanHoiKetQuaQuiz> nopBaiGoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maLopHoc, @PathVariable Long maBaiHoc,
            @Valid @RequestBody YeuCauNopBaiKiemTra yeuCau) {
        return ResponseEntity.ok(kiemTraTrongLopDichVu.nopBaiGoc(nd.getMaNguoiDung(), maLopHoc, maBaiHoc, yeuCau));
    }

    @PostMapping("/quiz/bo-sung/{maBaiHocBoSung}/nop-bai")
    public ResponseEntity<PhanHoiKetQuaQuiz> nopBaiBoSung(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maLopHoc, @PathVariable Long maBaiHocBoSung,
            @Valid @RequestBody YeuCauNopBaiKiemTra yeuCau) {
        return ResponseEntity.ok(kiemTraTrongLopDichVu.nopBaiBoSung(nd.getMaNguoiDung(), maLopHoc, maBaiHocBoSung, yeuCau));
    }

    @GetMapping("/quiz/goc/{maBaiHoc}/ket-qua")
    public ResponseEntity<PhanHoiKetQuaQuiz> layKetQuaGoc(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maLopHoc, @PathVariable Long maBaiHoc) {
        return kiemTraTrongLopDichVu.layKetQuaGanNhatGoc(nd.getMaNguoiDung(), maBaiHoc)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/quiz/bo-sung/{maBaiHocBoSung}/ket-qua")
    public ResponseEntity<PhanHoiKetQuaQuiz> layKetQuaBoSung(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maLopHoc, @PathVariable Long maBaiHocBoSung) {
        return kiemTraTrongLopDichVu.layKetQuaGanNhatBoSung(nd.getMaNguoiDung(), maBaiHocBoSung)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
