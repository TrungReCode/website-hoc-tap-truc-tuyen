package com.elearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * Lớp khởi chạy chính của Nền Tảng Học Tập Trực Tuyến Lai.
 * Kiến trúc: Modular Monolith - package-by-feature.
 *
 * @SpringBootApplication đặt ở root package "com.elearning" để @ComponentScan
 *                        quét được toàn bộ 5 module nghiệp vụ
 *                        (dinhDanhPhanQuyen, choKhoaHoc,
 *                        lopHocLMS, thanhToan, traoDoiTrongLop) và dungChung,
 *                        bao gồm cả các class
 *                        package-private bên trong từng module (Spring không
 *                        quan tâm access modifier
 *                        khi đăng ký bean, chỉ quan tâm annotation).
 */
@SpringBootApplication
@EnableScheduling
public class ElearningPlatformApplication {

    public static void main(String[] args) {
        // Đặt múi giờ mặc định của ứng dụng về UTC để tránh lỗi lệch múi giờ
        // Cài đặt múi giờ mặc định của ứng dụng theo giờ Việt Nam
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(ElearningPlatformApplication.class, args);
    }
}
