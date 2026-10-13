package com.elearning.dinhDanhPhanQuyen.giaoDien;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.TaiKhoanNganHangDto;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.ThongTinCaNhan;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauCapNhatHoSo;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDoiMatKhau;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauTaiKhoanNganHang;
import com.elearning.dinhDanhPhanQuyen.ungDung.HoSoCaNhanDichVu;
import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ho-so")
@RequiredArgsConstructor
public class HoSoCaNhanController {

    private final HoSoCaNhanDichVu hoSoCaNhanDichVu;

    @GetMapping("/me")
    public ResponseEntity<ThongTinCaNhan> layHoSo(@AuthenticationPrincipal NguoiDungThamChieu nguoiDung) {
        return ResponseEntity.ok(hoSoCaNhanDichVu.layHoSo(nguoiDung.getMaNguoiDung()));
    }

    @PutMapping("/me")
    public ResponseEntity<ThongTinCaNhan> capNhatHoSo(@AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
                                                     @Valid @RequestBody YeuCauCapNhatHoSo yeuCau) {
        return ResponseEntity.ok(hoSoCaNhanDichVu.capNhatHoSo(nguoiDung.getMaNguoiDung(), yeuCau));
    }

    @PostMapping("/doi-mat-khau")
    public ResponseEntity<Void> doiMatKhau(@AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
                                           @Valid @RequestBody YeuCauDoiMatKhau yeuCau) {
        hoSoCaNhanDichVu.doiMatKhau(nguoiDung.getMaNguoiDung(), yeuCau);
        return ResponseEntity.noContent().build();
    }

    // Bank accounts
    @GetMapping("/tai-khoan-ngan-hang")
    public ResponseEntity<List<TaiKhoanNganHangDto>> layTaiKhoan(@AuthenticationPrincipal NguoiDungThamChieu nguoiDung) {
        return ResponseEntity.ok(hoSoCaNhanDichVu.layTaiKhoanNganHang(nguoiDung.getMaNguoiDung()));
    }

    @PostMapping("/tai-khoan-ngan-hang")
    public ResponseEntity<TaiKhoanNganHangDto> themTaiKhoan(@AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
                                                           @Valid @RequestBody YeuCauTaiKhoanNganHang yeuCau) {
        TaiKhoanNganHangDto dto = hoSoCaNhanDichVu.themTaiKhoanNganHang(nguoiDung.getMaNguoiDung(), yeuCau);
        return ResponseEntity.status(201).body(dto);
    }

    @PutMapping("/tai-khoan-ngan-hang/{maTaiKhoan}")
    public ResponseEntity<TaiKhoanNganHangDto> capNhatTaiKhoan(@AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
                                                              @PathVariable Long maTaiKhoan,
                                                              @Valid @RequestBody YeuCauTaiKhoanNganHang yeuCau) {
        return ResponseEntity.ok(hoSoCaNhanDichVu.capNhatTaiKhoanNganHang(nguoiDung.getMaNguoiDung(), maTaiKhoan, yeuCau));
    }

    @DeleteMapping("/tai-khoan-ngan-hang/{maTaiKhoan}")
    public ResponseEntity<Void> xoaTaiKhoan(@AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
                                            @PathVariable Long maTaiKhoan) {
        hoSoCaNhanDichVu.xoaTaiKhoanNganHang(nguoiDung.getMaNguoiDung(), maTaiKhoan);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/tai-khoan-ngan-hang/{maTaiKhoan}/mac-dinh")
    public ResponseEntity<TaiKhoanNganHangDto> datLamMacDinh(@AuthenticationPrincipal NguoiDungThamChieu nguoiDung,
                                                              @PathVariable Long maTaiKhoan) {
        return ResponseEntity.ok(hoSoCaNhanDichVu.datLamMacDinh(nguoiDung.getMaNguoiDung(), maTaiKhoan));
    }
}