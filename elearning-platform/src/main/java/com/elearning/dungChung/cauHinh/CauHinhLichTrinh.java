package com.elearning.dungChung.cauHinh;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Kích hoạt @Scheduled cho toàn hệ thống. Dùng cho BoXuLyDoiSoatTuDong
 * (module thanhToan) quét các giao dịch đã hết han_giu_tien để đưa vào
 * đối soát hằng ngày.
 */
@Configuration
@EnableScheduling
public class CauHinhLichTrinh {
}
