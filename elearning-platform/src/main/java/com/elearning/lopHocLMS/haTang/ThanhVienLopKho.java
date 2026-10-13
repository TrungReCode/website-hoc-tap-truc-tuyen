package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.ThanhVienLop;
import com.elearning.lopHocLMS.mienDomain.ThanhVienLopId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThanhVienLopKho extends JpaRepository<ThanhVienLop, ThanhVienLopId> {

    List<ThanhVienLop> findByMaLopHoc(Long maLopHoc);

    List<ThanhVienLop> findByMaHocVien(Long maHocVien);

    long countByMaLopHoc(Long maLopHoc);

    boolean existsByMaHocVienAndMaLopHoc(Long maHocVien, Long maLopHoc);
}
