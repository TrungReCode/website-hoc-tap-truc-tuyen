package com.elearning.thanhToan.haTang;

import com.elearning.thanhToan.mienDomain.PhieuHoanTienKhoaHoc;
import com.elearning.thanhToan.mienDomain.TrangThaiHoanTien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhieuHoanTienKhoaHocKho extends JpaRepository<PhieuHoanTienKhoaHoc, Long> {

	boolean existsByMaPhieuGiaoDich(Long maPhieuGiaoDich);

	Optional<PhieuHoanTienKhoaHoc> findByMaPhieuGiaoDich(Long maPhieuGiaoDich);

	Page<PhieuHoanTienKhoaHoc> findByTrangThai(TrangThaiHoanTien trangThai, Pageable pageable);
}