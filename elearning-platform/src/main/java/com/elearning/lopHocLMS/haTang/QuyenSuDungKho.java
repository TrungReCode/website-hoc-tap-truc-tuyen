package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.QuyenSuDung;
import com.elearning.lopHocLMS.mienDomain.QuyenSuDungId;
import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiGiayPhep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuyenSuDungKho extends JpaRepository<QuyenSuDung, QuyenSuDungId> {

    Optional<QuyenSuDung> findByMaNguoiDungAndMaKhoaHoc(Long maNguoiDung, Long maKhoaHoc);

    boolean existsByMaNguoiDungAndMaKhoaHoc(Long maNguoiDung, Long maKhoaHoc);
    boolean existsByMaNguoiDungAndMaKhoaHocAndLoaiGiayPhep(Long maNguoiDung, Long maKhoaHoc, LoaiGiayPhep loaiGiayPhep);

    /** Có bất kỳ người dùng nào đang sở hữu khóa học này hay không (bất kể ai) — dùng để chặn xóa cứng nội dung đã bán. */
    boolean existsByMaKhoaHoc(Long maKhoaHoc);

    List<QuyenSuDung> findByMaNguoiDung(Long maNguoiDung);
}
