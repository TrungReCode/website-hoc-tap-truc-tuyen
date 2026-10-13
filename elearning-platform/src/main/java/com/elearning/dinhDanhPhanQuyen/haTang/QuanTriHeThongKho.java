package com.elearning.dinhDanhPhanQuyen.haTang;

import com.elearning.dinhDanhPhanQuyen.mienDomain.NguoiDung;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.TrangThaiTaiKhoan;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.VaiTro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Mở rộng NguoiDungKho với các query thống kê dành riêng cho admin.
 * Tách thành interface riêng để tránh bloat NguoiDungKho và rõ ràng ranh giới
 * trách nhiệm.
 */
public interface QuanTriHeThongKho extends JpaRepository<NguoiDung, Long> {

    Optional<NguoiDung> findByEmail(String email);

    boolean existsByEmail(String email);

    // ── Đếm theo tình trạng ──
    long countByTrangThai(TrangThaiTaiKhoan trangThai);

    long countByVaiTro(VaiTro vaiTro);

    // ── Tìm kiếm có lọc dành cho admin ──
    @Query("SELECT n FROM NguoiDung n WHERE " +
            "(:timKiem = '' OR LOWER(n.hoTen) LIKE :timKiem OR LOWER(n.email) LIKE :timKiem) " +
            "AND (:vaiTro = '' OR CAST(n.vaiTro AS String) = :vaiTro) " +
            "AND (:trangThai = '' OR CAST(n.trangThai AS String) = :trangThai)")
    Page<NguoiDung> timKiemNguoiDung(
            @Param("timKiem") String timKiem,
            @Param("vaiTro") String vaiTro, // Đổi thành String
            @Param("trangThai") String trangThai, // Đổi thành String
            Pageable pageable);

    // ── Thống kê nội dung (cross-table native SQL) ──
    @Query(value = "SELECT COUNT(*) FROM khoa_hoc", nativeQuery = true)
    long demTongKhoaHoc();

    @Query(value = "SELECT COUNT(*) FROM khoa_hoc WHERE trang_thai = 'CONG_KHAI'", nativeQuery = true)
    long demKhoaHocCongKhai();

    @Query(value = "SELECT COUNT(*) FROM khoa_hoc WHERE trang_thai = 'CHO_KIEM_DUYET'", nativeQuery = true)
    long demKhoaHocChoDuyet();

    @Query(value = "SELECT COUNT(*) FROM khoa_hoc WHERE trang_thai = 'NHAP'", nativeQuery = true)
    long demKhoaHocNhap();

    @Query(value = "SELECT COUNT(*) FROM lop_hoc", nativeQuery = true)
    long demTongLopHoc();

    @Query(value = "SELECT COUNT(*) FROM lop_hoc WHERE trang_thai = 'HOAT_DONG'", nativeQuery = true)
    long demLopHocHoatDong();

    // ── Thống kê tài chính (native SQL) ──
    @Query(value = "SELECT COALESCE(SUM(so_tien),0) FROM phieu_mua_khoa_hoc WHERE trang_thai = 'THANH_CONG'", nativeQuery = true)
    BigDecimal tongDoanhThuKhoaHoc();

    @Query(value = "SELECT COALESCE(SUM(so_tien),0) FROM phieu_tra_phi_lop_hoc WHERE trang_thai = 'THANH_CONG'", nativeQuery = true)
    BigDecimal tongDoanhThuLopHoc();

    @Query(value = "SELECT COUNT(*) FROM phieu_mua_khoa_hoc WHERE trang_thai = 'THANH_CONG'", nativeQuery = true)
    long demGiaoDichKhoaHocThanhCong();

    @Query(value = "SELECT COUNT(*) FROM phieu_tra_phi_lop_hoc WHERE trang_thai = 'THANH_CONG'", nativeQuery = true)
    long demGiaoDichLopHocThanhCong();

    @Query(value = "SELECT COUNT(*) FROM phieu_mua_khoa_hoc WHERE trang_thai = 'CHO_XAC_NHAN'", nativeQuery = true)
    long demGiaoDichKhoaHocCho();

    @Query(value = "SELECT COUNT(*) FROM phieu_tra_phi_lop_hoc WHERE trang_thai = 'CHO_XAC_NHAN'", nativeQuery = true)
    long demGiaoDichLopHocCho();

    @Query(value = "SELECT COUNT(*) FROM phieu_doi_soat WHERE trang_thai IN ('CHO_XAC_NHAN','DANG_XU_LY')", nativeQuery = true)
    long demPhieuDoiSoatChuaXuLy();

    // ── Tương tác ──
    @Query(value = "SELECT COUNT(*) FROM bai_dang WHERE xoa_mem = FALSE", nativeQuery = true)
    long demTongBaiDang();

    @Query(value = "SELECT COUNT(*) FROM binh_luan", nativeQuery = true)
    long demTongBinhLuan();
}
