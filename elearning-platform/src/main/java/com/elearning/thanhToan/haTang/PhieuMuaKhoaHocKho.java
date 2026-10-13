package com.elearning.thanhToan.haTang;

import com.elearning.thanhToan.mienDomain.PhieuMuaKhoaHoc;
import com.elearning.thanhToan.mienDomain.TrangThaiDoiSoat;
import com.elearning.thanhToan.mienDomain.TrangThaiGiaoDich;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhieuMuaKhoaHocKho extends JpaRepository<PhieuMuaKhoaHoc, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PhieuMuaKhoaHoc p WHERE p.maGiaoDichVnpay = :maGiaoDichVnpay")
    Optional<PhieuMuaKhoaHoc> timKhoaVaKhoa(@Param("maGiaoDichVnpay") String maGiaoDichVnpay);

    Optional<PhieuMuaKhoaHoc> findByMaGiaoDichVnpay(String maGiaoDichVnpay);

    boolean existsByMaNguoiDungAndMaKhoaHocAndTrangThai(Long maNguoiDung, Long maKhoaHoc, TrangThaiGiaoDich trangThai);

    List<PhieuMuaKhoaHoc> findByMaNguoiDungOrderByNgayTaoDesc(Long maNguoiDung);

    List<PhieuMuaKhoaHoc> findByMaKhoaHocIn(List<Long> maKhoaHocList);

    /** Ứng viên cho đối soát tự động — chỉ giao dịch thành công và chưa lập phiếu. */
    List<PhieuMuaKhoaHoc> findByTrangThaiAndMaDoiSoatIsNull(TrangThaiGiaoDich trangThai);

    /**
     * Bản tối ưu của findByTrangThaiAndMaDoiSoatIsNotNull — lọc thẳng theo
     * đúng trangThaiDoiSoat ngay ở CSDL (subquery) thay vì kéo về TOÀN BỘ
     * giao dịch đã có phiếu (kể cả đã DA_GIAI_NGAN/THAT_BAI từ lâu) rồi mới
     * lọc bằng Java trong BoXuLyDoiSoatTuDong — giảm số bản ghi phải quét
     * mỗi lượt chạy @Scheduled khi dữ liệu lớn dần.
     */
    @Query("SELECT p FROM PhieuMuaKhoaHoc p WHERE p.trangThai = :trangThaiGiaoDich "
            + "AND p.maDoiSoat IN (SELECT d.maDoiSoat FROM PhieuDoiSoat d WHERE d.trangThai = :trangThaiDoiSoat)")
    List<PhieuMuaKhoaHoc> timGiaoDichCanXetGiaiNgan(
            @Param("trangThaiGiaoDich") TrangThaiGiaoDich trangThaiGiaoDich,
            @Param("trangThaiDoiSoat") TrangThaiDoiSoat trangThaiDoiSoat);

    /** Toàn bộ giao dịch thành công trong hệ thống — dùng cho báo cáo doanh thu của Quản trị viên. */
    List<PhieuMuaKhoaHoc> findByTrangThai(TrangThaiGiaoDich trangThai);

    /** Drill-down: các giao dịch đã được gộp vào một phiếu đối soát cụ thể. */
    List<PhieuMuaKhoaHoc> findByMaDoiSoat(Long maDoiSoat);
}