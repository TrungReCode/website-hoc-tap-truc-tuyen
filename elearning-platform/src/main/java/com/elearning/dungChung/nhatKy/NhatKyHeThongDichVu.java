package com.elearning.dungChung.nhatKy;

import java.time.LocalDate;

/**
 * PUBLIC — Đọc lại audit log do GhiNhatKyHanhDong ghi ra file.
 * Chỉ được gọi từ AdminController, bảo vệ bằng @PreAuthorize ở tầng controller
 * (QTV_CAO_CAP), vì nhật ký hành động quản trị là dữ liệu nhạy cảm.
 */
public interface NhatKyHeThongDichVu {

    /**
     * @param ngay   ngày cần xem (null = hôm nay)
     * @param soDong số dòng gần nhất muốn lấy (đã bị chặn tối đa ở tầng service)
     * @param tuKhoa lọc theo từ khóa xuất hiện trong dòng log (null/rỗng = không lọc)
     */
    PhanHoiNhatKy xemNhatKy(LocalDate ngay, int soDong, String tuKhoa);
}
