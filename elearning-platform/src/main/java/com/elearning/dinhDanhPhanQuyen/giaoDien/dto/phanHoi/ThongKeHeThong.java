package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi;

import java.math.BigDecimal;

/**
 * Snapshot thống kê tổng quan hệ thống — trả về cho QTV_CAO_CAP.
 * Mọi con số tính tại thời điểm request (không cache), đủ cho dashboard
 * quan sát sức khoẻ hệ thống theo thời gian thực.
 */
public record ThongKeHeThong(

        // ── Người dùng ──
        long tongNguoiDung,
        long nguoiDungHoatDong,
        long nguoiDungBiCam,
        long nguoiDungChoXacNhan,
        long qtvNoiDung,
        long qtvTaiChinh,

        // ── Nội dung ──
        long tongKhoaHoc,
        long khoaHocCongKhai,
        long khoaHocChoDuyet,
        long khoaHocNhap,
        long tongLopHoc,
        long lopHocDangHoatDong,

        // ── Tài chính (aggregate từ bảng phiếu) ──
        BigDecimal tongDoanhThuKhoaHoc,
        BigDecimal tongDoanhThuLopHoc,
        long soGiaoDichThanhCong,
        long soGiaoDichChoXacNhan,
        long soPhieuDoiSoatChuaXuLy,

        // ── Tương tác ──
        long tongBaiDang,
        long tongBinhLuan
) {}
