package com.elearning.choKhoaHoc.ungDung.impl;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhMuc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhMucQuanTri;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauCapNhatDanhMuc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoDanhMuc;
import com.elearning.choKhoaHoc.haTang.DanhMucKho;
import com.elearning.choKhoaHoc.haTang.KhoaHocKho;
import com.elearning.choKhoaHoc.mienDomain.DanhMuc;
import com.elearning.choKhoaHoc.ungDung.DanhMucDichVu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UC-13 (một phần) Quản trị hệ thống — quản lý danh mục khóa học cho
 * Quản trị viên nội dung. Chỉ hỗ trợ phân cấp 2 mức (danh mục gốc → con)
 * đúng như thiết kế CSDL (muc_cha tự tham chiếu, không giới hạn độ sâu ở
 * tầng DB nhưng nghiệp vụ chỉ dùng 2 mức).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class DanhMucDichVuImpl implements DanhMucDichVu {

    private final DanhMucKho danhMucKho;
    private final KhoaHocKho khoaHocKho;

    @Override
    public List<PhanHoiDanhMuc> layTatCa() {
        return danhMucKho.findAll().stream().map(this::sangPhanHoi).toList();
    }

    @Override
    public List<PhanHoiDanhMucQuanTri> layTatCaChoQuanTri() {
        return danhMucKho.findAll().stream().map(this::sangPhanHoiQuanTri).toList();
    }

    @Override
    @Transactional
    public PhanHoiDanhMucQuanTri taoDanhMuc(YeuCauTaoDanhMuc yeuCau) {
        if (danhMucKho.existsByTenDanhMucIgnoreCase(yeuCau.tenDanhMuc())) {
            throw new NgoaiLeDuLieuKhongHopLe("Tên danh mục này đã tồn tại");
        }

        DanhMuc mucCha = layMucChaHopLe(yeuCau.maDanhMucCha());

        DanhMuc d = new DanhMuc(yeuCau.tenDanhMuc(), mucCha);
        danhMucKho.save(d);
        return sangPhanHoiQuanTri(d);
    }

    @Override
    @Transactional
    public PhanHoiDanhMucQuanTri capNhatDanhMuc(Long maDanhMuc, YeuCauCapNhatDanhMuc yeuCau) {
        DanhMuc d = danhMucKho.findById(maDanhMuc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("danh mục", maDanhMuc));

        if (!d.getTenDanhMuc().equalsIgnoreCase(yeuCau.tenDanhMuc())
                && danhMucKho.existsByTenDanhMucIgnoreCase(yeuCau.tenDanhMuc())) {
            throw new NgoaiLeDuLieuKhongHopLe("Tên danh mục này đã tồn tại");
        }

        if (yeuCau.maDanhMucCha() != null && yeuCau.maDanhMucCha().equals(maDanhMuc)) {
            throw new NgoaiLeDuLieuKhongHopLe("Danh mục không thể là danh mục cha của chính nó");
        }

        // Danh mục đang có con thì không thể "hạ cấp" thành danh mục con của danh mục khác
        // (tránh phá vỡ giới hạn phân cấp 2 mức: cha -> con -> cháu).
        if (yeuCau.maDanhMucCha() != null && danhMucKho.countByMucCha_MaDanhMuc(maDanhMuc) > 0) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Danh mục này đang có danh mục con nên không thể trở thành danh mục con của danh mục khác");
        }

        DanhMuc mucCha = layMucChaHopLe(yeuCau.maDanhMucCha());

        d.setTenDanhMuc(yeuCau.tenDanhMuc());
        d.setMucCha(mucCha);
        return sangPhanHoiQuanTri(d);
    }

    @Override
    @Transactional
    public void xoaDanhMuc(Long maDanhMuc) {
        DanhMuc d = danhMucKho.findById(maDanhMuc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("danh mục", maDanhMuc));

        if (khoaHocKho.countByDanhMuc_MaDanhMuc(maDanhMuc) > 0) {
            throw new NgoaiLeDuLieuKhongHopLe("Không thể xóa: vẫn còn khóa học đang thuộc danh mục này");
        }
        if (danhMucKho.countByMucCha_MaDanhMuc(maDanhMuc) > 0) {
            throw new NgoaiLeDuLieuKhongHopLe("Không thể xóa: vẫn còn danh mục con bên trong danh mục này");
        }

        danhMucKho.delete(d);
    }

    // ---------- Nội bộ ----------

    private DanhMuc layMucChaHopLe(Long maDanhMucCha) {
        if (maDanhMucCha == null) {
            return null;
        }
        DanhMuc mucCha = danhMucKho.findById(maDanhMucCha)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("danh mục cha", maDanhMucCha));
        if (mucCha.getMucCha() != null) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Chỉ hỗ trợ phân cấp 2 mức — không thể chọn một danh mục con làm danh mục cha");
        }
        return mucCha;
    }

    private PhanHoiDanhMuc sangPhanHoi(DanhMuc d) {
        return new PhanHoiDanhMuc(
                d.getMaDanhMuc(),
                d.getTenDanhMuc(),
                d.getMucCha() != null ? d.getMucCha().getMaDanhMuc() : null
        );
    }

    private PhanHoiDanhMucQuanTri sangPhanHoiQuanTri(DanhMuc d) {
        return new PhanHoiDanhMucQuanTri(
                d.getMaDanhMuc(),
                d.getTenDanhMuc(),
                d.getMucCha() != null ? d.getMucCha().getMaDanhMuc() : null,
                d.getMucCha() != null ? d.getMucCha().getTenDanhMuc() : null,
                khoaHocKho.countByDanhMuc_MaDanhMuc(d.getMaDanhMuc()),
                danhMucKho.countByMucCha_MaDanhMuc(d.getMaDanhMuc())
        );
    }
}