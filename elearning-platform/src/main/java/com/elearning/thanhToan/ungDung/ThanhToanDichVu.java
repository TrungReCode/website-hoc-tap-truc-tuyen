package com.elearning.thanhToan.ungDung;

import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiGiaoDich;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiGiaoDichNhan;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTaoDonThanhToan;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiYeuCauHoanTien;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiXuLyVnpay;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauHoanTien;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauThanhToanKhoaHoc;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauThanhToanLopHoc;

import java.util.List;
import java.util.Map;

public interface ThanhToanDichVu {

    PhanHoiTaoDonThanhToan taoDonMuaKhoaHoc(Long maNguoiDung, Long maKhoaHoc, YeuCauThanhToanKhoaHoc yeuCau,
            String originNguoiDung);

    PhanHoiTaoDonThanhToan taoDonPhiLopHoc(Long maNguoiDung, String maLop, YeuCauThanhToanLopHoc yeuCau,
            String originNguoiDung);

    PhanHoiYeuCauHoanTien taoYeuCauHoanTienKhoaHoc(Long maNguoiDung, Long maPhieuGiaoDich, YeuCauHoanTien yeuCau);

    PhanHoiYeuCauHoanTien taoYeuCauHoanTienLopHoc(Long maNguoiDung, Long maPhieuGiaoDich, YeuCauHoanTien yeuCau);

    /** UC lịch sử giao dịch — gộp cả khóa học và lớp học, sắp xếp mới nhất trước. */
    List<PhanHoiGiaoDich> layLichSuGiaoDich(Long maNguoiDung);

    /**
     * Lịch sử giao dịch NHẬN tiền — với vai trò tác giả khóa học/giảng viên
     * phụ trách lớp, dựa trên các phiếu đối soát (PhieuDoiSoat) liên kết
     * tới khóa học/lớp do người này sở hữu/phụ trách.
     */
    List<PhanHoiGiaoDichNhan> layLichSuGiaoDichNhan(Long maNguoiDung);

    PhanHoiXuLyVnpay xuLyReturn(Map<String, String> params);

    PhanHoiXuLyVnpay xuLyIpn(Map<String, String> params);
}