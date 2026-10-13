package com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi;

import com.elearning.traoDoiTrongLop.mienDomain.TepDinhKem;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiBaiDang;

import java.time.LocalDateTime;
import java.util.List;

public record PhanHoiBaiDang(
        Long maBaiDang,
        Long maLopHoc,
        Long maNguoiDang,
        String tenNguoiDang,
        String anhDaiDienNguoiDang,
        LoaiBaiDang loai,
        String tieuDe,
        String noiDung,
        List<TepDinhKem> tepDinhKem,
        Boolean giaiDap,
        long soBinhLuan,
        LocalDateTime ngayTao,
        LocalDateTime ngayCapNhat
) {}
