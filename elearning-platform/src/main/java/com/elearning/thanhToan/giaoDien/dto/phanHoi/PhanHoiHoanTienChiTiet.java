package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import com.elearning.thanhToan.mienDomain.TrangThaiHoanTien;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Chi tiết một yêu cầu hoàn tiền dành cho màn hình quản trị (QTV_CAO_CAP / QTV_TAI_CHINH):
 * gộp thông tin của phiếu hoàn tiền với người yêu cầu và đối tượng giao dịch gốc.
 */
public record PhanHoiHoanTienChiTiet(
        Long maHoanTien,
        Long maPhieuGiaoDich,
        String loaiGiaoDich,       // "KHOA_HOC" | "LOP_HOC"
        Long maNguoiDung,
        String tenNguoiDung,
        String emailNguoiDung,
        String tieuDeDoiTuong,
        String maGiaoDichVnpay,
        BigDecimal soTien,
        String lyDo,
        TrangThaiHoanTien trangThai,
        String ghiChuAdmin,
        LocalDateTime ngayTao,
        LocalDateTime ngayXuLy
) {
}