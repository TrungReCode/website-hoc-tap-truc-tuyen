package com.elearning.dungChung.cauHinh;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Kích hoạt @Async cho toàn hệ thống. Dùng cho:
 * - Gửi email kích hoạt/reset mật khẩu (module IAM) không block luồng HTTP chính
 * - ThongBaoDichVu gửi thông báo (module dungChung.thongBao) chạy nền sau khi
 *   các module nghiệp vụ publish event (thanh toán thành công, lời mời được
 *   chấp nhận...)
 */
@Configuration
@EnableAsync
public class CauHinhAsync {
}
