package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi;

import java.math.BigDecimal;

/**
 * Tỷ lệ phí tách riêng theo loại giấy phép (LoaiGiayPhep):
 * - tyLePhiCaNhan  áp dụng cho giao dịch mua Giấy phép cá nhân (CA_NHAN)
 * - tyLePhiDayHoc  áp dụng cho giao dịch mua Giấy phép giảng dạy (DAY_HOC)
 *                  VÀ cho phí lớp học (PhieuTraPhiLopHoc) — lớp học chỉ mở
 *                  được khi giảng viên đã có Giấy phép giảng dạy, nên về bản
 *                  chất doanh thu phí lớp cùng nhóm với DAY_HOC.
 *
 * Trước đây chỉ có 1 trường tyLePhiDichVu dùng chung cho mọi giao dịch —
 * đây chính là lỗi khiến phí nền tảng tính sai cho Giấy phép giảng dạy (xem
 * BoXuLyDoiSoatTuDong.chonTyLePhi()).
 */
public record PhanHoiCauHinhHeThong(
        BigDecimal tyLePhiCaNhan,          // % hoa hồng nền tảng cho Giấy phép cá nhân
        BigDecimal tyLePhiDayHoc,          // % hoa hồng nền tảng cho Giấy phép giảng dạy + phí lớp học
        Integer soNgayGiuKyQuyKhoaHoc,     // số ngày giữ tiền trước khi đối soát cho khóa học
        Integer soNgayGiuKyQuyLopHoc       // số ngày giữ tiền trước khi đối soát cho lớp học
) {
}