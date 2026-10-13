package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.KetQuaQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KetQuaQuizKho extends JpaRepository<KetQuaQuiz, Long> {

    List<KetQuaQuiz> findByMaNguoiDungAndMaBaiKiemTraOrderByThoiDiemLamDesc(Long maNguoiDung, Long maBaiKiemTra);

    /** Dùng bởi chungChi: kết quả quiz của 1 học viên, giới hạn trong 1 lớp cụ thể
     *  (cho Giấy xác nhận hoàn thành lớp học — không tính điểm làm ở lớp khác). */
    List<KetQuaQuiz> findByMaNguoiDungAndLopHoc_MaLopHocAndMaBaiKiemTra(
            Long maNguoiDung, Long maLopHoc, Long maBaiKiemTra);
}
