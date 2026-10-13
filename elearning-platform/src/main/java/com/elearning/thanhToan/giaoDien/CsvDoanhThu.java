package com.elearning.thanhToan.giaoDien;

import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiGiaoDichDoanhThu;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Xuất danh sách giao dịch doanh thu ra CSV (UC-16, bước "xuất báo cáo dạng
 * CSV"). Có BOM UTF-8 ở đầu file để Excel trên Windows đọc đúng tiếng Việt
 * có dấu thay vì hiển thị ký tự lỗi.
 */
final class CsvDoanhThu {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final byte[] BOM_UTF8 = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

    private CsvDoanhThu() {
    }

    static byte[] xuat(List<PhanHoiGiaoDichDoanhThu> danhSach, boolean coCotGiangVien) {
        StringBuilder sb = new StringBuilder();
        if (coCotGiangVien) {
            sb.append("Mã phiếu,Loại,Ngày giao dịch,Khóa học,Lớp,Giảng viên,Số tiền (VNĐ),Trạng thái\n");
        } else {
            sb.append("Mã phiếu,Loại,Ngày giao dịch,Khóa học,Lớp,Số tiền (VNĐ),Trạng thái\n");
        }

        for (PhanHoiGiaoDichDoanhThu gd : danhSach) {
            sb.append(gd.maPhieu()).append(',');
            sb.append(gd.loai().equals("KHOA_HOC") ? "Mua khóa học" : "Phí lớp học").append(',');
            sb.append(gd.ngayGiaoDich().format(DINH_DANG_NGAY)).append(',');
            sb.append(thoat(gd.tenKhoaHoc())).append(',');
            sb.append(thoat(gd.maLop() != null ? gd.maLop() : "")).append(',');
            if (coCotGiangVien) {
                sb.append(thoat(gd.tenGiangVien() != null ? gd.tenGiangVien() : "")).append(',');
            }
            sb.append(gd.soTien().toPlainString()).append(',');
            sb.append(gd.trangThai()).append('\n');
        }

        byte[] noiDung = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] ketQua = new byte[BOM_UTF8.length + noiDung.length];
        System.arraycopy(BOM_UTF8, 0, ketQua, 0, BOM_UTF8.length);
        System.arraycopy(noiDung, 0, ketQua, BOM_UTF8.length, noiDung.length);
        return ketQua;
    }

    /** Escape dấu phẩy/ngoặc kép/xuống dòng theo chuẩn CSV (bọc trong ngoặc kép khi cần). */
    private static String thoat(String gt) {
        if (gt == null) return "";
        boolean canBoc = gt.contains(",") || gt.contains("\"") || gt.contains("\n");
        String daThoat = gt.replace("\"", "\"\"");
        return canBoc ? "\"" + daThoat + "\"" : daThoat;
    }
}
