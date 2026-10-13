package com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi;

import com.elearning.traoDoiTrongLop.mienDomain.TepDinhKem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PhanHoiBinhLuan(
        Long maBinhLuan,
        Long maBaiDang,
        Long maNguoiDung,
        String tenNguoiDung,
        String anhDaiDien,
        Long maCha,
        String noiDung,
        LocalDateTime ngayTao,
        /** Danh sách reply (chỉ có khi maCha = NULL — bình luận gốc). */
        List<PhanHoiBinhLuan> danhSachReply,

        /**
         * "BINH_LUAN" (mặc định, bài đăng thường) | "NOP_BAI" (bài nộp của học
         * viên trên bài đăng loại BAI_TAP) | "CHAM_DIEM" (phản hồi chấm điểm
         * của giảng viên cho một bài nộp cụ thể).
         */
        String loaiBinhLuan,

        /** Chỉ có giá trị khi loaiBinhLuan = "NOP_BAI". */
        List<TepDinhKem> tepDinhKemNopBai,

        /** Chỉ có giá trị khi loaiBinhLuan = "CHAM_DIEM". */
        BigDecimal diem,
        BigDecimal diemToiDa
) {}
