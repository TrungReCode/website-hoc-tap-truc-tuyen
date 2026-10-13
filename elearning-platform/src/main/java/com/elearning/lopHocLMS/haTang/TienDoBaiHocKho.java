package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.TienDoBaiHoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TienDoBaiHocKho extends JpaRepository<TienDoBaiHoc, Long> {

    // ─── Tự học (maLopHoc IS NULL) ───────────────────────────────────────
    Optional<TienDoBaiHoc> findByMaHocVienAndMaBaiHocAndMaLopHocIsNull(Long maHocVien, Long maBaiHoc);

    List<TienDoBaiHoc> findByMaHocVienAndMaBaiHocInAndMaLopHocIsNull(Long maHocVien, List<Long> dsMaBaiHoc);

    long countByMaHocVienAndMaBaiHocInAndMaLopHocIsNullAndHoanThanhTrue(Long maHocVien, List<Long> dsMaBaiHoc);

    // ─── Riêng theo 1 lớp học cụ thể (maLopHoc = X) ──────────────────────
    Optional<TienDoBaiHoc> findByMaHocVienAndMaBaiHocAndMaLopHoc(Long maHocVien, Long maBaiHoc, Long maLopHoc);

    long countByMaHocVienAndMaBaiHocInAndMaLopHocAndHoanThanhTrue(Long maHocVien, List<Long> dsMaBaiHoc, Long maLopHoc);
}