package com.elearning.lopHocLMS.haTang.congViecTuDong;

import com.elearning.lopHocLMS.haTang.LopHocKho;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CapNhatTrangThaiLopJob {

    private final LopHocKho lopHocKho;

    // Chạy tự động vào phút thứ 0 của mỗi giờ (VD: 1:00, 2:00, 3:00...)
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void capNhatTrangThaiLopHocTuDong() {
        LocalDateTime bayGio = LocalDateTime.now();

        // 1. Cập nhật các lớp SẮP DIỄN RA -> HOAT DONG (Khi đã đến ngày bắt đầu)
        lopHocKho.capNhatTrangThai(
            TrangThaiLop.SAP_DIEN_RA, 
            TrangThaiLop.HOAT_DONG, 
            bayGio, 
            "BAT_DAU"
        );

        // 2. Cập nhật các lớp HOAT DONG -> ĐÃ KẾT THÚC (Khi đã qua ngày kết thúc)
        lopHocKho.capNhatTrangThai(
            TrangThaiLop.HOAT_DONG, 
            TrangThaiLop.DA_DONG, 
            bayGio, 
            "KET_THUC"
        );
    }
}