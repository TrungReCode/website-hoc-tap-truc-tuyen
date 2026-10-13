package com.elearning.traoDoiTrongLop.ungDung.impl;

import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.dungChung.phanTrang.KetQuaPhanTrang;
import com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi.PhanHoiThongBao;
import com.elearning.traoDoiTrongLop.haTang.ThongBaoKho;
import com.elearning.traoDoiTrongLop.mienDomain.ThongBao;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;
import com.elearning.traoDoiTrongLop.ungDung.ThongBaoDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
class ThongBaoDichVuImpl implements ThongBaoDichVu {

    private final ThongBaoKho thongBaoKho;

    @Override
    public void guiThongBao(Long maNguoiDung, LoaiThongBao loai, String tieuDe, String noiDung,
                             Map<String, Object> duLieuDong) {
        thongBaoKho.save(new ThongBao(maNguoiDung, loai, tieuDe, noiDung, duLieuDong));
    }

    @Override
    @Transactional(readOnly = true)
    public KetQuaPhanTrang<PhanHoiThongBao> layThongBaoCuaToi(Long maNguoiDung, Pageable pageable) {
        return KetQuaPhanTrang.tuPage(
                thongBaoKho.findByMaNguoiDungOrderByNgayTaoDesc(maNguoiDung, pageable)
                        .map(this::sangPhanHoi));
    }

    @Override
    @Transactional(readOnly = true)
    public long demChuaDoc(Long maNguoiDung) {
        return thongBaoKho.countByMaNguoiDungAndTrangThaiDocFalse(maNguoiDung);
    }

    @Override
    public void danhDauTatCaDaDoc(Long maNguoiDung) {
        thongBaoKho.danhDauTatCaDaDoc(maNguoiDung);
    }

    @Override
    public void danhDauDaDoc(Long maNguoiDung, Long maThongBao) {
        ThongBao tb = thongBaoKho.findById(maThongBao)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("thông báo", maThongBao));
        if (!tb.getMaNguoiDung().equals(maNguoiDung)) {
            throw new NgoaiLeKhongCoQuyen("Bạn không có quyền đánh dấu thông báo này");
        }
        tb.danhDauDaDoc();
    }

    private PhanHoiThongBao sangPhanHoi(ThongBao tb) {
        return new PhanHoiThongBao(
                tb.getMaThongBao(), tb.getLoai(), tb.getTieuDe(),
                tb.getNoiDung(), tb.getTrangThaiDoc(), tb.getThongBaoTuongTac(),
                tb.getNgayTao(), tb.getDuLieuDong());
    }
}