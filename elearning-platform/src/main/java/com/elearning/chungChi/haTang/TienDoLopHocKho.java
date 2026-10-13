package com.elearning.chungChi.haTang;

import com.elearning.chungChi.mienDomain.TienDoLopHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TienDoLopHocKho extends JpaRepository<TienDoLopHoc, Long> {
    Optional<TienDoLopHoc> findByMaHocVienAndMaLopHoc(Long maHocVien, Long maLopHoc);
    List<TienDoLopHoc> findByMaHocVien(Long maHocVien);
}
