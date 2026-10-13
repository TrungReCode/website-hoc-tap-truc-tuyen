package com.elearning.chungChi.haTang;

import com.elearning.chungChi.mienDomain.GiayXacNhanCaNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GiayXacNhanCaNhanKho extends JpaRepository<GiayXacNhanCaNhan, Long> {
    Optional<GiayXacNhanCaNhan> findByMaHocVienAndMaKhoaHoc(Long maHocVien, Long maKhoaHoc);
    Optional<GiayXacNhanCaNhan> findByMaXacThuc(String maXacThuc);
    List<GiayXacNhanCaNhan> findByMaHocVien(Long maHocVien);
}
