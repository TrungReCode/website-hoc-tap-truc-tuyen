package com.elearning.chungChi.haTang;

import com.elearning.chungChi.mienDomain.GiayXacNhanLopHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GiayXacNhanLopHocKho extends JpaRepository<GiayXacNhanLopHoc, Long> {
    Optional<GiayXacNhanLopHoc> findByMaHocVienAndMaLopHoc(Long maHocVien, Long maLopHoc);
    Optional<GiayXacNhanLopHoc> findByMaXacThuc(String maXacThuc);
    List<GiayXacNhanLopHoc> findByMaHocVien(Long maHocVien);
}
