package com.elearning.dungChung.ngoaiLe;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Lớp cơ sở cho tất cả ngoại lệ nghiệp vụ trong hệ thống.
 * Mọi exception nghiệp vụ cụ thể (NgoaiLeKhongTimThay, NgoaiLeKhongCoQuyen...)
 * nên kế thừa từ lớp này để XuLyNgoaiLeToanCuc có thể bắt và xử lý đồng nhất.
 */
public class NgoaiLeNghiepVu extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String maLoi;
    /** Dữ liệu bổ sung tùy chọn để FE điều hướng/xử lý đúng ngữ cảnh lỗi
     *  (VD: maKhoaHoc cần mua) mà không phải parse message dạng chuỗi.
     *  NULL cho phần lớn lỗi thông thường, chỉ set khi thật sự cần thiết. */
    private final Map<String, Object> duLieu;

    public NgoaiLeNghiepVu(String thongBao, HttpStatus httpStatus, String maLoi) {
        this(thongBao, httpStatus, maLoi, null);
    }

    public NgoaiLeNghiepVu(String thongBao, HttpStatus httpStatus, String maLoi, Map<String, Object> duLieu) {
        super(thongBao);
        this.httpStatus = httpStatus;
        this.maLoi = maLoi;
        this.duLieu = duLieu;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMaLoi() {
        return maLoi;
    }

    public Map<String, Object> getDuLieu() {
        return duLieu;
    }
}