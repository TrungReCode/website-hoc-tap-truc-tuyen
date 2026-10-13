package com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi;

import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;

import java.time.LocalDateTime;
import java.util.Map;

public record PhanHoiThongBao(
        Long maThongBao,
        LoaiThongBao loai,
        String tieuDe,
        String noiDung,
        Boolean trangThaiDoc,
        Boolean thongBaoTuongTac,
        LocalDateTime ngayTao,
        Map<String, Object> duLieuDong
) {}