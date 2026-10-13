package com.elearning.dinhDanhPhanQuyen.ungDung.impl;

import com.elearning.dinhDanhPhanQuyen.haTang.NguoiDungKho;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.VaiTro;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.TrangThaiTaiKhoan;
import com.elearning.dinhDanhPhanQuyen.ungDung.NguoiDungTraCuuDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class NguoiDungTraCuuDichVuImpl implements NguoiDungTraCuuDichVu {

    private final NguoiDungKho nguoiDungKho;

    @Override
    public List<Long> layDanhSachMaNguoiDungTheoVaiTro(VaiTro vaiTro) {
        return nguoiDungKho.layMaTheoVaiTro(vaiTro, TrangThaiTaiKhoan.HOAT_DONG);
    }
}