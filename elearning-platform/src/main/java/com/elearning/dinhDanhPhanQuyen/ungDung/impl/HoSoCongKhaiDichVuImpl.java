package com.elearning.dinhDanhPhanQuyen.ungDung.impl;

import com.elearning.dinhDanhPhanQuyen.haTang.NguoiDungKho;
import com.elearning.dinhDanhPhanQuyen.mienDomain.NguoiDung;
import com.elearning.dinhDanhPhanQuyen.ungDung.HoSoCongKhaiDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.ThongTinNguoiDungCoBan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class HoSoCongKhaiDichVuImpl implements HoSoCongKhaiDichVu {

    private final NguoiDungKho nguoiDungKho;

    @Override
    public Optional<ThongTinNguoiDungCoBan> layThongTinCoBan(Long maNguoiDung) {
        return nguoiDungKho.findById(maNguoiDung).map(this::sangDto);
    }

    @Override
    public Map<Long, ThongTinNguoiDungCoBan> layThongTinCoBanNhieu(Collection<Long> dsMaNguoiDung) {
        return nguoiDungKho.findAllById(dsMaNguoiDung).stream()
                .map(this::sangDto)
                .collect(Collectors.toMap(ThongTinNguoiDungCoBan::maNguoiDung, Function.identity()));
    }

    @Override
    public Optional<String> layEmail(Long maNguoiDung) {
        return nguoiDungKho.findById(maNguoiDung).map(NguoiDung::getEmail);
    }

    @Override
    public Map<Long, String> layEmailNhieu(Collection<Long> dsMaNguoiDung) {
        return nguoiDungKho.findAllById(dsMaNguoiDung).stream()
                .collect(Collectors.toMap(NguoiDung::getMaNguoiDung, NguoiDung::getEmail));
    }

    private ThongTinNguoiDungCoBan sangDto(NguoiDung nguoiDung) {
        return new ThongTinNguoiDungCoBan(nguoiDung.getMaNguoiDung(), nguoiDung.getHoTen(), nguoiDung.getAnhDaiDien());
    }
}
