package com.elearning.lopHocLMS.ungDung;

import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiDanhGiaLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauDanhGiaLop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DanhGiaLopHocDichVu {

    PhanHoiDanhGiaLop taoDanhGia(Long maNguoiDung, Long maLopHoc, YeuCauDanhGiaLop yeuCau);

    PhanHoiDanhGiaLop capNhatDanhGia(Long maNguoiDung, Long maDanhGia, YeuCauDanhGiaLop yeuCau);

    void xoaDanhGia(Long maNguoiDung, Long maDanhGia);

    Page<PhanHoiDanhGiaLop> layDanhGiaTheoLop(Long maLopHoc, Pageable pageable);

    Optional<PhanHoiDanhGiaLop> layDanhGiaCuaToi(Long maNguoiDung, Long maLopHoc);
}
