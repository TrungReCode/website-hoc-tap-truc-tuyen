package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.KetQuaQuizBoSung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KetQuaQuizBoSungKho extends JpaRepository<KetQuaQuizBoSung, Long> {

    List<KetQuaQuizBoSung> findByMaNguoiDungAndBaiKiemTraBoSung_MaBaiKiemTraBoSungOrderByThoiDiemLamDesc(
            Long maNguoiDung, Long maBaiKiemTraBoSung);
}
