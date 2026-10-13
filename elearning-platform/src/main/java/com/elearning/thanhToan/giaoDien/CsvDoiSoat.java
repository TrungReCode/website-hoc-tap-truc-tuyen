package com.elearning.thanhToan.giaoDien;

import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiPhieuDoiSoat;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Xuất toàn bộ danh sách phiếu đối soát (đã lọc, không phân trang) ra CSV —
 * "Export/Import bắt buộc phải có" cho Màn Hình Đối Soát. Có BOM UTF-8 để
 * Excel đọc đúng tiếng Việt có dấu.
 */
final class CsvDoiSoat {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final byte[] BOM_UTF8 = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

    private CsvDoiSoat() {
    }

    static byte[] xuat(List<PhanHoiPhieuDoiSoat> danhSach) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mã đối soát,Giảng viên,Chủ tài khoản,Số tài khoản,Ngân hàng,")
                .append("Doanh thu (VNĐ),Hoa hồng (%),Thực nhận (VNĐ),Chênh lệch (VNĐ),")
                .append("Trạng thái,Mã GD ngân hàng,Ngày tạo,Ngày chuyển thành công\n");

        for (PhanHoiPhieuDoiSoat p : danhSach) {
            sb.append(p.maDoiSoat()).append(',');
            sb.append(thoat(p.tenGiangVien())).append(',');
            sb.append(thoat(p.tenChuTaiKhoan())).append(',');
            sb.append(thoat(p.soTaiKhoan())).append(',');
            sb.append(thoat(p.tenNganHang())).append(',');
            sb.append(p.tongDoanhThu() != null ? p.tongDoanhThu().toPlainString() : "").append(',');
            sb.append(p.hoaHong() != null ? p.hoaHong().toPlainString() : "").append(',');
            sb.append(p.soTienThucTe() != null ? p.soTienThucTe().toPlainString() : "").append(',');
            sb.append(p.chenhLech() != null ? p.chenhLech().toPlainString() : "").append(',');
            sb.append(p.trangThai()).append(',');
            sb.append(thoat(p.maGiaoDichNganHang())).append(',');
            sb.append(p.ngayTao() != null ? p.ngayTao().format(DINH_DANG_NGAY) : "").append(',');
            sb.append(p.ngayChuyenThanhCong() != null ? p.ngayChuyenThanhCong().format(DINH_DANG_NGAY) : "").append('\n');
        }

        byte[] noiDung = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] ketQua = new byte[BOM_UTF8.length + noiDung.length];
        System.arraycopy(BOM_UTF8, 0, ketQua, 0, BOM_UTF8.length);
        System.arraycopy(noiDung, 0, ketQua, BOM_UTF8.length, noiDung.length);
        return ketQua;
    }

    private static String thoat(String gt) {
        if (gt == null) return "";
        boolean canBoc = gt.contains(",") || gt.contains("\"") || gt.contains("\n");
        String daThoat = gt.replace("\"", "\"\"");
        return canBoc ? "\"" + daThoat + "\"" : daThoat;
    }
}
