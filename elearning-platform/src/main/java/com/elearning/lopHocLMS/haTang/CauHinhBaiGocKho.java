package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.CauHinhBaiGoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CauHinhBaiGocKho extends JpaRepository<CauHinhBaiGoc, Long> {

    List<CauHinhBaiGoc> findByLopHoc_MaLopHoc(Long maLopHoc);

    Optional<CauHinhBaiGoc> findByLopHoc_MaLopHocAndMaBaiHoc(Long maLopHoc, Long maBaiHoc);
}
