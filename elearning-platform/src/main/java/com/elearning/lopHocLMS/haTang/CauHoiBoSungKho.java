package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.CauHoiBoSung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CauHoiBoSungKho extends JpaRepository<CauHoiBoSung, Long> {

    List<CauHoiBoSung> findByBaiKiemTraBoSung_MaBaiKiemTraBoSungOrderByViTriAsc(Long maBaiKiemTraBoSung);
}
