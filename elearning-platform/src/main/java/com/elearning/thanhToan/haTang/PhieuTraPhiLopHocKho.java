package com.elearning.thanhToan.haTang;

import com.elearning.thanhToan.mienDomain.PhieuTraPhiLopHoc;
import com.elearning.thanhToan.mienDomain.TrangThaiDoiSoat;
import com.elearning.thanhToan.mienDomain.TrangThaiGiaoDich;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhieuTraPhiLopHocKho extends JpaRepository<PhieuTraPhiLopHoc, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PhieuTraPhiLopHoc p WHERE p.maGiaoDichVnpay = :maGiaoDichVnpay")
    Optional<PhieuTraPhiLopHoc> timKhoaVaKhoa(@Param("maGiaoDichVnpay") String maGiaoDichVnpay);

    Optional<PhieuTraPhiLopHoc> findByMaGiaoDichVnpay(String maGiaoDichVnpay);

    boolean existsByMaNguoiDungAndMaLopHocAndTrangThai(Long maNguoiDung, Long maLopHoc, TrangThaiGiaoDich trangThai);

    List<PhieuTraPhiLopHoc> findByMaNguoiDungOrderByNgayTaoDesc(Long maNguoiDung);

    List<PhieuTraPhiLopHoc> findByMaLopHocIn(List<Long> maLopHocList);

    /** Ứng viên cho đối soát tự động — chỉ giao dịch thành công và chưa lập phiếu. */
    List<PhieuTraPhiLopHoc> findByTrangThaiAndMaDoiSoatIsNull(TrangThaiGiaoDich trangThai);

    /** Bản tối ưu — lọc thẳng theo trangThaiDoiSoat ở CSDL, xem PhieuMuaKhoaHocKho.timGiaoDichCanXetGiaiNgan. */
    @Query("SELECT p FROM PhieuTraPhiLopHoc p WHERE p.trangThai = :trangThaiGiaoDich "
            + "AND p.maDoiSoat IN (SELECT d.maDoiSoat FROM PhieuDoiSoat d WHERE d.trangThai = :trangThaiDoiSoat)")
    List<PhieuTraPhiLopHoc> timGiaoDichCanXetGiaiNgan(
            @Param("trangThaiGiaoDich") TrangThaiGiaoDich trangThaiGiaoDich,
            @Param("trangThaiDoiSoat") TrangThaiDoiSoat trangThaiDoiSoat);

    /** Dùng để chặn sửa học phí khi lớp đang trong chu kỳ giải ngân tài chính (đã có phiếu được đối soát). */
    boolean existsByMaLopHocAndMaDoiSoatIsNotNull(Long maLopHoc);

    /** Dùng để tự động hoàn phụ phí cho toàn bộ học viên đã đóng khi giảng viên hủy lớp chưa khai giảng. */
    List<PhieuTraPhiLopHoc> findByMaLopHocAndTrangThai(Long maLopHoc, TrangThaiGiaoDich trangThai);

    /** Dùng để chặn "Đóng lớp" (dongLop) bỏ qua hoàn tiền khi lớp chưa khai giảng đã có người trả phí —
     *  trường hợp này phải đi qua xoaLop/huyLopChuaKhaiGiang để đảm bảo phát sinh hoàn tiền đúng. */
    boolean existsByMaLopHocAndTrangThai(Long maLopHoc, TrangThaiGiaoDich trangThai);

    /** Toàn bộ giao dịch thành công trong hệ thống — dùng cho báo cáo doanh thu của Quản trị viên. */
    List<PhieuTraPhiLopHoc> findByTrangThai(TrangThaiGiaoDich trangThai);

    /** Drill-down: các giao dịch đã được gộp vào một phiếu đối soát cụ thể. */
    List<PhieuTraPhiLopHoc> findByMaDoiSoat(Long maDoiSoat);
}