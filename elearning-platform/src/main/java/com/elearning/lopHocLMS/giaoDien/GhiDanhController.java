package com.elearning.lopHocLMS.giaoDien;

import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLoiMoi;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLop;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiThanhVienLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauMoiHocVien;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauThamGiaBangMa;
import com.elearning.lopHocLMS.ungDung.GhiDanhDichVu;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GhiDanhController {

    private final GhiDanhDichVu ghiDanhDichVu;

    /** Tham gia lớp bằng mã — S13. */
    @PostMapping("/api/lop-hoc/tham-gia")
    public ResponseEntity<PhanHoiLop> thamGiaBangMa(@AuthenticationPrincipal NguoiDungThamChieu nd,
                                                     @Valid @RequestBody YeuCauThamGiaBangMa yeuCau) {
        return ResponseEntity.ok(ghiDanhDichVu.thamGiaBangMa(nd.getMaNguoiDung(), yeuCau));
    }

    /** Lời mời chờ xác nhận của người dùng hiện tại — dùng email từ UserDetails. */
    @GetMapping("/api/loi-moi/cua-toi")
    public ResponseEntity<List<PhanHoiLoiMoi>> layLoiMoiCuaToi(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ghiDanhDichVu.layLoiMoiCuaToi(nd.getMaNguoiDung(), userDetails.getUsername()));
    }

    @PostMapping("/api/loi-moi/{maLoiMoi}/chap-nhan")
    public ResponseEntity<PhanHoiLop> chapNhanLoiMoi(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long maLoiMoi) {
        return ResponseEntity.ok(ghiDanhDichVu.chapNhanLoiMoi(nd.getMaNguoiDung(), userDetails.getUsername(), maLoiMoi));
    }

    @PostMapping("/api/loi-moi/{maLoiMoi}/tu-choi")
    public ResponseEntity<Void> tuChoiLoiMoi(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long maLoiMoi) {
        ghiDanhDichVu.tuChoiLoiMoi(nd.getMaNguoiDung(), userDetails.getUsername(), maLoiMoi);
        return ResponseEntity.noContent().build();
    }

    /** Giảng viên mời học viên — S27. */
    @PostMapping("/api/lop-hoc/{maLopHoc}/moi")
    public ResponseEntity<List<PhanHoiLoiMoi>> moiHocVien(
            @AuthenticationPrincipal NguoiDungThamChieu nd,
            @PathVariable Long maLopHoc,
            @Valid @RequestBody YeuCauMoiHocVien yeuCau) {
        return ResponseEntity.status(201).body(ghiDanhDichVu.moiHocVien(nd.getMaNguoiDung(), maLopHoc, yeuCau));
    }

    @GetMapping("/api/lop-hoc/{maLopHoc}/loi-moi")
    public ResponseEntity<List<PhanHoiLoiMoi>> layLoiMoiCuaLop(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maLopHoc) {
        return ResponseEntity.ok(ghiDanhDichVu.layLoiMoiCuaLop(nd.getMaNguoiDung(), maLopHoc));
    }

    @GetMapping("/api/lop-hoc/{maLopHoc}/thanh-vien")
    public ResponseEntity<List<PhanHoiThanhVienLop>> layThanhVien(
            @AuthenticationPrincipal NguoiDungThamChieu nd, @PathVariable Long maLopHoc) {
        return ResponseEntity.ok(ghiDanhDichVu.layThanhVien(nd.getMaNguoiDung(), maLopHoc));
    }
}
