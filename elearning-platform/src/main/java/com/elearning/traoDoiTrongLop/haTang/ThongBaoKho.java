package com.elearning.traoDoiTrongLop.haTang;

import com.elearning.traoDoiTrongLop.mienDomain.ThongBao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThongBaoKho extends JpaRepository<ThongBao, Long> {

    Page<ThongBao> findByMaNguoiDungOrderByNgayTaoDesc(Long maNguoiDung, Pageable pageable);

    long countByMaNguoiDungAndTrangThaiDocFalse(Long maNguoiDung);

    @Modifying
    @Query("UPDATE ThongBao t SET t.trangThaiDoc = true WHERE t.maNguoiDung = :maNguoiDung AND t.trangThaiDoc = false")
    int danhDauTatCaDaDoc(@Param("maNguoiDung") Long maNguoiDung);
}
