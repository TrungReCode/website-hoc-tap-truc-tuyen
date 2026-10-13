package com.elearning.dinhDanhPhanQuyen.haTang;

import com.elearning.dinhDanhPhanQuyen.mienDomain.XacThucGiangVien;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.TrangThaiXacThucGiangVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface XacThucGiangVienKho extends JpaRepository<XacThucGiangVien, Long> {

    Optional<XacThucGiangVien> findByMaNguoiDung(Long maNguoiDung);

    boolean existsByMaNguoiDungAndTrangThai(Long maNguoiDung, TrangThaiXacThucGiangVien trangThai);

    Page<XacThucGiangVien> findByTrangThai(TrangThaiXacThucGiangVien trangThai, Pageable pageable);
}
