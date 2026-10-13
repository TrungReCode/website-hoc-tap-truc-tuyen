package com.elearning.thanhToan.giaoDien.dto.phanHoi;

import java.math.BigDecimal;
import java.util.List;

public record PhanHoiXuHuongDoanhThu(
        List<DiemXuHuong> diem
) {
    /** @param nhan nhãn hiển thị trên biểu đồ, dạng "yyyy-MM" (theo tháng) */
    public record DiemXuHuong(String nhan, BigDecimal doanhThu) {
    }
}
