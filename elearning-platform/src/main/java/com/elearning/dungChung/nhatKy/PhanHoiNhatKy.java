package com.elearning.dungChung.nhatKy;

import java.time.LocalDate;
import java.util.List;

/**
 * Phản hồi cho API xem nhật ký (audit log) — chỉ dành cho QTV_CAO_CAP.
 * File nguồn: logs/audit.log (hôm nay) hoặc logs/audit-{ngay}.log (ngày cũ),
 * cấu hình xoay vòng tại logback-spring.xml.
 *
 * Đặt ở dungChung.nhatKy (không phải dinhDanhPhanQuyen) vì đây là DTO gắn
 * với hạ tầng ghi/đọc log dùng chung, không phải nghiệp vụ IAM — tránh để
 * dungChung phụ thuộc ngược vào module nghiệp vụ.
 */
public record PhanHoiNhatKy(
        LocalDate ngay,
        boolean coFileLog,
        int soDongTraVe,
        List<MucNhatKy> danhSach
) {
    /**
     * 1 dòng nhật ký đã được phân tích từ định dạng ghi trong
     * GhiNhatKyHanhDong: "actor=... action=... target=... detail=..."
     */
    public record MucNhatKy(
            String thoiGian,
            String nguoiThucHien,
            String hanhDong,
            String doiTuong,
            String chiTiet
    ) {
    }
}
