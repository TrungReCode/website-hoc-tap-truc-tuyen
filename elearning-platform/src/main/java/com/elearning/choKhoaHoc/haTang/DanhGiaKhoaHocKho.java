package com.elearning.choKhoaHoc.haTang;

import com.elearning.choKhoaHoc.mienDomain.DanhGiaKhoaHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DanhGiaKhoaHocKho extends JpaRepository<DanhGiaKhoaHoc, Long> {

    Page<DanhGiaKhoaHoc> findByKhoaHoc_MaKhoaHocOrderByNgayTaoDesc(Long maKhoaHoc, Pageable pageable);

    /** Danh sách công khai — loại trừ các đánh giá đã bị QTV nội dung ẩn do vi phạm. */
    Page<DanhGiaKhoaHoc> findByKhoaHoc_MaKhoaHocAndAnFalseOrderByNgayTaoDesc(Long maKhoaHoc, Pageable pageable);

    Optional<DanhGiaKhoaHoc> findByMaNguoiDungAndKhoaHoc_MaKhoaHoc(Long maNguoiDung, Long maKhoaHoc);

    @Query("SELECT AVG(d.soSao) FROM DanhGiaKhoaHoc d WHERE d.khoaHoc.maKhoaHoc = :maKhoaHoc AND d.an = false")
    Double tinhDiemTrungBinh(@Param("maKhoaHoc") Long maKhoaHoc);

    long countByKhoaHoc_MaKhoaHoc(Long maKhoaHoc);

    long countByKhoaHoc_MaKhoaHocAndAnFalse(Long maKhoaHoc);
}
