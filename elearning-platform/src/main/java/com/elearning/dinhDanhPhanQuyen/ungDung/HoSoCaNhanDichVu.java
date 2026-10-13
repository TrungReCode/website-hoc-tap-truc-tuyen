package com.elearning.dinhDanhPhanQuyen.ungDung;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.TaiKhoanNganHangDto;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.ThongTinCaNhan;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauCapNhatHoSo;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDoiMatKhau;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauTaiKhoanNganHang;

import java.util.List;

public interface HoSoCaNhanDichVu {

    ThongTinCaNhan layHoSo(Long maNguoiDung);

    ThongTinCaNhan capNhatHoSo(Long maNguoiDung, YeuCauCapNhatHoSo yeuCau);

    void doiMatKhau(Long maNguoiDung, YeuCauDoiMatKhau yeuCau);

    List<TaiKhoanNganHangDto> layTaiKhoanNganHang(Long maNguoiDung);

    TaiKhoanNganHangDto themTaiKhoanNganHang(Long maNguoiDung, YeuCauTaiKhoanNganHang yeuCau);

    TaiKhoanNganHangDto capNhatTaiKhoanNganHang(Long maNguoiDung, Long maTaiKhoan, YeuCauTaiKhoanNganHang yeuCau);

    void xoaTaiKhoanNganHang(Long maNguoiDung, Long maTaiKhoan);

    /** Đặt một tài khoản ngân hàng làm mặc định — tự động bỏ mặc định các tài khoản khác của cùng giảng viên. */
    TaiKhoanNganHangDto datLamMacDinh(Long maNguoiDung, Long maTaiKhoan);
}