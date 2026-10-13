package com.elearning.thanhToan.haTang;

import com.elearning.thanhToan.mienDomain.PhieuHoanTienLopHoc;
import com.elearning.thanhToan.mienDomain.TrangThaiHoanTien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhieuHoanTienLopHocKho extends JpaRepository<PhieuHoanTienLopHoc, Long> {

	boolean existsByMaPhieuGiaoDich(Long maPhieuGiaoDich);

	Optional<PhieuHoanTienLopHoc> findByMaPhieuGiaoDich(Long maPhieuGiaoDich);

	Page<PhieuHoanTienLopHoc> findByTrangThai(TrangThaiHoanTien trangThai, Pageable pageable);
}