package com.elearning.dinhDanhPhanQuyen.ungDung.impl;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.TaiKhoanNganHangDto;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.ThongTinCaNhan;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauCapNhatHoSo;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDoiMatKhau;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauTaiKhoanNganHang;
import com.elearning.dinhDanhPhanQuyen.haTang.MaLamMoiPhienKho;
import com.elearning.dinhDanhPhanQuyen.haTang.NguoiDungKho;
import com.elearning.dinhDanhPhanQuyen.haTang.TaiKhoanNganHangKho;
import com.elearning.dinhDanhPhanQuyen.mienDomain.MaLamMoiPhien;
import com.elearning.dinhDanhPhanQuyen.mienDomain.NguoiDung;
import com.elearning.dinhDanhPhanQuyen.mienDomain.TaiKhoanNganHang;
import com.elearning.dinhDanhPhanQuyen.ungDung.HoSoCaNhanDichVu;
import com.elearning.thanhToan.haTang.PhieuDoiSoatKho;
import com.elearning.thanhToan.mienDomain.PhieuDoiSoat;
import com.elearning.thanhToan.mienDomain.TrangThaiDoiSoat;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class HoSoCaNhanDichVuImpl implements HoSoCaNhanDichVu {

    private final NguoiDungKho nguoiDungKho;
    private final PasswordEncoder passwordEncoder;
    private final MaLamMoiPhienKho maLamMoiPhienKho;
    private final TaiKhoanNganHangKho taiKhoanNganHangKho;
    private final PhieuDoiSoatKho phieuDoiSoatKho;

    @Override
    @Transactional(readOnly = true)
    public ThongTinCaNhan layHoSo(Long maNguoiDung) {
        NguoiDung nd = nguoiDungKho.findById(maNguoiDung).orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        return new ThongTinCaNhan(nd.getMaNguoiDung(), nd.getHoTen(), nd.getEmail(), nd.getSdt(), nd.getAnhDaiDien(), nd.getTieuSu());
    }

    @Override
    @Transactional
    public ThongTinCaNhan capNhatHoSo(Long maNguoiDung, YeuCauCapNhatHoSo yeuCau) {
        NguoiDung nd = nguoiDungKho.findById(maNguoiDung).orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        if (yeuCau.hoTen() != null && !yeuCau.hoTen().isBlank()) nd.setHoTen(yeuCau.hoTen());
        nd.setSdt(yeuCau.sdt());
        nd.setAnhDaiDien(yeuCau.anhDaiDien());
        nd.setTieuSu(yeuCau.tieuSu());
        nguoiDungKho.save(nd);
        return new ThongTinCaNhan(nd.getMaNguoiDung(), nd.getHoTen(), nd.getEmail(), nd.getSdt(), nd.getAnhDaiDien(), nd.getTieuSu());
    }

    @Override
    @Transactional
    public void doiMatKhau(Long maNguoiDung, YeuCauDoiMatKhau yeuCau) {
        if (!Objects.equals(yeuCau.matKhauMoi(), yeuCau.xacNhanMatKhauMoi())) {
            throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp");
        }
        NguoiDung nd = nguoiDungKho.findById(maNguoiDung).orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        if (!passwordEncoder.matches(yeuCau.matKhauHienTai(), nd.getMatKhau())) {
            throw new NgoaiLeDuLieuKhongHopLe("Mật khẩu hiện tại không đúng");
        }
        if (passwordEncoder.matches(yeuCau.matKhauMoi(), nd.getMatKhau())) {
            throw new NgoaiLeDuLieuKhongHopLe("Mật khẩu mới không được trùng mật khẩu hiện tại");
        }
        // Kiểm tra độ mạnh cơ bản
        if (yeuCau.matKhauMoi().length() < 8) throw new NgoaiLeDuLieuKhongHopLe("Mật khẩu mới quá ngắn");

        nd.setMatKhau(passwordEncoder.encode(yeuCau.matKhauMoi()));
        nguoiDungKho.save(nd);

        // Thu hồi tất cả refresh token của người này
        List<MaLamMoiPhien> tokens = maLamMoiPhienKho.findByNguoiDungId(maNguoiDung);
        tokens.forEach(t -> t.setThuHoi(true));
        maLamMoiPhienKho.saveAll(tokens);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaiKhoanNganHangDto> layTaiKhoanNganHang(Long maNguoiDung) {
        return taiKhoanNganHangKho.findByMaGiangVien(maNguoiDung).stream()
                .map(t -> new TaiKhoanNganHangDto(t.getMaTaiKhoan(), t.getMaGiangVien(), t.getTenChuTaiKhoan(), t.getSoTaiKhoan(), t.getTenNganHang(), t.getMacDinh()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaiKhoanNganHangDto themTaiKhoanNganHang(Long maNguoiDung, YeuCauTaiKhoanNganHang yeuCau) {
        TaiKhoanNganHang tk = new TaiKhoanNganHang(maNguoiDung, yeuCau.tenChuTaiKhoan(), yeuCau.soTaiKhoan(), yeuCau.tenNganHang());
        // Constructor mặc định luôn set macDinh=true — chỉ tài khoản ĐẦU TIÊN của
        // giảng viên mới thực sự nên là mặc định, các tài khoản thêm sau phải giữ false.
        boolean daCoTaiKhoan = !taiKhoanNganHangKho.findByMaGiangVien(maNguoiDung).isEmpty();
        if (daCoTaiKhoan) {
            tk.setMacDinh(false);
        }
        taiKhoanNganHangKho.save(tk);
        return new TaiKhoanNganHangDto(tk.getMaTaiKhoan(), tk.getMaGiangVien(), tk.getTenChuTaiKhoan(), tk.getSoTaiKhoan(), tk.getTenNganHang(), tk.getMacDinh());
    }

    @Override
    @Transactional
    public TaiKhoanNganHangDto capNhatTaiKhoanNganHang(Long maNguoiDung, Long maTaiKhoan, YeuCauTaiKhoanNganHang yeuCau) {
        TaiKhoanNganHang tk = taiKhoanNganHangKho.findById(maTaiKhoan).orElseThrow(() -> new IllegalArgumentException("Tài khoản ngân hàng không tồn tại"));
        if (!tk.getMaGiangVien().equals(maNguoiDung)) throw new IllegalArgumentException("Không có quyền sửa tài khoản này");

        // Ràng buộc an toàn tài chính: áp dụng cho cả sửa lẫn xóa (xem
        // xoaTaiKhoanNganHang) — không cho đổi thông tin nhận tiền khi đang
        // có khoản giải ngân chờ xử lý trỏ tới chính tài khoản này.
        kiemTraKhongCoPhieuDoiSoatDangCho(tk, "sửa");

        tk.setTenChuTaiKhoan(yeuCau.tenChuTaiKhoan());
        tk.setSoTaiKhoan(yeuCau.soTaiKhoan());
        tk.setTenNganHang(yeuCau.tenNganHang());
        taiKhoanNganHangKho.save(tk);
        return new TaiKhoanNganHangDto(tk.getMaTaiKhoan(), tk.getMaGiangVien(), tk.getTenChuTaiKhoan(), tk.getSoTaiKhoan(), tk.getTenNganHang(), tk.getMacDinh());
    }

    @Override
    @Transactional
    public void xoaTaiKhoanNganHang(Long maNguoiDung, Long maTaiKhoan) {
        TaiKhoanNganHang tk = taiKhoanNganHangKho.findById(maTaiKhoan).orElseThrow(() -> new IllegalArgumentException("Tài khoản ngân hàng không tồn tại"));
        if (!tk.getMaGiangVien().equals(maNguoiDung)) throw new IllegalArgumentException("Không có quyền xóa tài khoản này");

        kiemTraKhongCoPhieuDoiSoatDangCho(tk, "xóa");

        taiKhoanNganHangKho.delete(tk);
    }

    /**
     * Ràng buộc an toàn tài chính dùng chung cho sửa/xóa tài khoản ngân hàng:
     * chặn thao tác nếu đang tồn tại phiếu đối soát thu nhập ở trạng thái
     * chờ xử lý trỏ tới chính tài khoản này.
     */
    private void kiemTraKhongCoPhieuDoiSoatDangCho(TaiKhoanNganHang tk, String hanhDong) {
        List<PhieuDoiSoat> phieus = phieuDoiSoatKho.findByTrangThaiIn(List.of(TrangThaiDoiSoat.CHO_XAC_NHAN, TrangThaiDoiSoat.DANG_XU_LY));
        boolean coPhieuCho = phieus.stream().anyMatch(p -> Objects.equals(p.getMaTaiKhoan(), tk.getMaTaiKhoan()));
        if (coPhieuCho) {
            throw new IllegalStateException(
                    "Không thể " + hanhDong + " tài khoản ngân hàng khi đang có khoản giải ngân đang chờ xử lý");
        }
    }

    @Override
    @Transactional
    public TaiKhoanNganHangDto datLamMacDinh(Long maNguoiDung, Long maTaiKhoan) {
        TaiKhoanNganHang tk = taiKhoanNganHangKho.findById(maTaiKhoan)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản ngân hàng không tồn tại"));
        if (!tk.getMaGiangVien().equals(maNguoiDung)) {
            throw new IllegalArgumentException("Không có quyền sửa tài khoản này");
        }

        List<TaiKhoanNganHang> tatCa = taiKhoanNganHangKho.findByMaGiangVien(maNguoiDung);
        tatCa.forEach(t -> t.setMacDinh(t.getMaTaiKhoan().equals(maTaiKhoan)));
        taiKhoanNganHangKho.saveAll(tatCa);

        return new TaiKhoanNganHangDto(tk.getMaTaiKhoan(), tk.getMaGiangVien(), tk.getTenChuTaiKhoan(), tk.getSoTaiKhoan(), tk.getTenNganHang(), true);
    }
}