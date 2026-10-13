package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.BaiKiemTraBoSung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BaiKiemTraBoSungKho extends JpaRepository<BaiKiemTraBoSung, Long> {

    Optional<BaiKiemTraBoSung> findByBaiBoSung_MaBaiHocBoSung(Long maBaiHocBoSung);
}
