package com.elearning.thanhToan.haTang;

import com.elearning.thanhToan.mienDomain.PhieuDoiSoat;
import com.elearning.thanhToan.mienDomain.TrangThaiDoiSoat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PhieuDoiSoatKho extends JpaRepository<PhieuDoiSoat, Long> {

    Page<PhieuDoiSoat> findByTrangThaiIn(Collection<TrangThaiDoiSoat> trangThais, Pageable pageable);

    List<PhieuDoiSoat> findByTrangThaiIn(List<TrangThaiDoiSoat> trangThais);

    long countByTrangThai(TrangThaiDoiSoat trangThai);

    List<PhieuDoiSoat> findByMaTaiKhoanIn(List<Long> maTaiKhoanList);

    Page<PhieuDoiSoat> findByMaTaiKhoanInAndTrangThai(List<Long> maTaiKhoanList, TrangThaiDoiSoat trangThai, Pageable pageable);

    Page<PhieuDoiSoat> findByMaTaiKhoanIn(List<Long> maTaiKhoanList, Pageable pageable);
}