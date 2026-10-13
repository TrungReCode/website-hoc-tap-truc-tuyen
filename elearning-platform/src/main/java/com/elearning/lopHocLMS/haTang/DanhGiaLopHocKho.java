package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.DanhGiaLopHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DanhGiaLopHocKho extends JpaRepository<DanhGiaLopHoc, Long> {

    Page<DanhGiaLopHoc> findByLopHoc_MaLopHocOrderByNgayTaoDesc(Long maLopHoc, Pageable pageable);

    Optional<DanhGiaLopHoc> findByMaHocVienAndLopHoc_MaLopHoc(Long maHocVien, Long maLopHoc);

    @Query("SELECT AVG(d.soSao) FROM DanhGiaLopHoc d WHERE d.lopHoc.maLopHoc = :maLopHoc")
    Double tinhDiemTrungBinh(@Param("maLopHoc") Long maLopHoc);

    long countByLopHoc_MaLopHoc(Long maLopHoc);
}
