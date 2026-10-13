package com.elearning.dungChung.ngoaiLe;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Cấu trúc JSON chuẩn trả về cho client khi có lỗi, để Frontend (React)
 * xử lý đồng nhất ở interceptor Axios, không cần parse nhiều format khác nhau.
 */
public record PhanHoiLoi(
        LocalDateTime thoiGian,
        int trangThaiHttp,
        String maLoi,
        String thongBao,
        String duongDan,
        /** Dữ liệu bổ sung tùy chọn (VD: {"maKhoaHoc": 17}) để FE điều hướng
         *  đúng ngữ cảnh thay vì phải đoán/parse thongBao dạng chuỗi. NULL với
         *  hầu hết lỗi thông thường không cần thêm ngữ cảnh gì. */
        Map<String, Object> duLieu
) {
    public static PhanHoiLoi tao(int trangThaiHttp, String maLoi, String thongBao, String duongDan) {
        return new PhanHoiLoi(LocalDateTime.now(), trangThaiHttp, maLoi, thongBao, duongDan, null);
    }

    public static PhanHoiLoi tao(int trangThaiHttp, String maLoi, String thongBao, String duongDan,
            Map<String, Object> duLieu) {
        return new PhanHoiLoi(LocalDateTime.now(), trangThaiHttp, maLoi, thongBao, duongDan, duLieu);
    }
}