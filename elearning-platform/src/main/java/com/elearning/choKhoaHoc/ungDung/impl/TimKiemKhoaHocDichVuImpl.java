package com.elearning.choKhoaHoc.ungDung.impl;

import com.elearning.choKhoaHoc.haTang.KhoaHocKho;
import com.elearning.choKhoaHoc.mienDomain.KhoaHoc;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.TrangThaiKhoaHoc;
import com.elearning.choKhoaHoc.ungDung.ThongTinKhoaHocCoBan;
import com.elearning.choKhoaHoc.ungDung.TimKiemKhoaHocDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class TimKiemKhoaHocDichVuImpl implements TimKiemKhoaHocDichVu {

    private final KhoaHocKho khoaHocKho;

    @Override
    public boolean laTacGiaKhoaHoc(Long maNguoiDung, Long maKhoaHoc) {
        return khoaHocKho.findById(maKhoaHoc)
                .map(k -> k.laTacGia(maNguoiDung))
                .orElse(false);
    }

    @Override
    public boolean khoaHocDangCongKhai(Long maKhoaHoc) {
        return khoaHocKho.findByMaKhoaHocAndTrangThai(maKhoaHoc, TrangThaiKhoaHoc.CONG_KHAI).isPresent();
    }

    @Override
    public Optional<ThongTinKhoaHocCoBan> layThongTinCoBan(Long maKhoaHoc) {
        return khoaHocKho.findById(maKhoaHoc).map(this::sangDto);
    }

    @Override
    public List<Long> timMaKhoaHocKhopTuKhoa(String tuKhoa) {
        return khoaHocKho.timMaKhoaHocCongKhaiKhopTuKhoa(tuKhoa, TrangThaiKhoaHoc.CONG_KHAI);
    }

    private ThongTinKhoaHocCoBan sangDto(KhoaHoc k) {
        return new ThongTinKhoaHocCoBan(
                k.getMaKhoaHoc(), k.getMaTacGia(), k.getTieuDe(), k.getAnhBia(), k.getGia(), k.getPhiDayHoc(), k.getTrangThai());
    }
}