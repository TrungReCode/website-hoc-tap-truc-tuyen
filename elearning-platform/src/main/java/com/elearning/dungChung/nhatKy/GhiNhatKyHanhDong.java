package com.elearning.dungChung.nhatKy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Ghi nhật ký (audit log) cho các hành động nhạy cảm — UC-13 Quản trị hệ
 * thống yêu cầu "toàn bộ hành động nhạy cảm được ghi nhật ký".
 *
 * Vì đồ án chủ động KHÔNG thêm bảng CSDL mới cho việc này, log được ghi ra
 * file riêng (logs/audit.log, cấu hình tại logback-spring.xml) thay vì lưu
 * trong Postgres. Đây là cách tiếp cận hợp lệ và khá phổ biến trong thực tế
 * (audit trail dạng structured log) khi không muốn/không thể đổi schema.
 *
 * Đặt ở dungChung vì mọi module (dinhDanhPhanQuyen, choKhoaHoc, thanhToan...)
 * đều có thể có hành động nhạy cảm cần ghi nhận.
 */
@Component
public class GhiNhatKyHanhDong {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * @param maNguoiDungThucHien mã người dùng (thường là Quản trị viên) thực hiện hành động
     * @param hanhDong            tên hành động, dạng SNAKE_CASE ngắn gọn, vd "KHOA_TAI_KHOAN"
     * @param doiTuong            đối tượng bị tác động, vd "nguoi_dung#42"
     * @param chiTiet             mô tả chi tiết thêm (lý do, giá trị cũ/mới...), có thể null
     */
    public void ghiNhan(Long maNguoiDungThucHien, String hanhDong, String doiTuong, String chiTiet) {
        AUDIT.info("actor=nguoi_dung#{} action={} target={} detail={}",
                maNguoiDungThucHien, hanhDong, doiTuong, chiTiet == null ? "-" : chiTiet);
    }
}
