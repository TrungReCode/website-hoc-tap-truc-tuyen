package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau;

public record YeuCauDuyetXacThucGiangVien(
        boolean chapDuyet,
        String lyDoTuChoi // bắt buộc có ý nghĩa khi chapDuyet = false, service tự kiểm tra
) {
}
