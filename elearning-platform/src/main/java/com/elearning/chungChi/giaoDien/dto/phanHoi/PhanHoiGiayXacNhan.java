package com.elearning.chungChi.giaoDien.dto.phanHoi;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PhanHoiGiayXacNhan(
        Long maGiay,
        String maXacThuc,
        Long maHocVien,
        /** maKhoaHoc (giấy cá nhân) hoặc maLopHoc (giấy lớp học) */
        Long maTaiNguyen,
        /** "CA_NHAN" | "LOP_HOC" */
        String loai,
        BigDecimal phanTramHoanThanh,
        BigDecimal diemTrungBinh,
        LocalDateTime ngayCap,
        String filePdfUrl,
        String qrCodeUrl,
        String trangThai
) {}
