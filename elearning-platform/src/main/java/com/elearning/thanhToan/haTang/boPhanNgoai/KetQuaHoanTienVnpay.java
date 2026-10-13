package com.elearning.thanhToan.haTang.boPhanNgoai;

public record KetQuaHoanTienVnpay(
        boolean thanhCong,
        String maPhanHoi,   // vnp_ResponseCode
        String thongDiep    // mô tả lỗi/kết quả để hiển thị cho admin
) {
}