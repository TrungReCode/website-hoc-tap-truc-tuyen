package com.elearning.traoDoiTrongLop.ungDung;

import com.elearning.dungChung.phanTrang.KetQuaPhanTrang;
import com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi.PhanHoiThongBao;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * PUBLIC — Quản lý thông báo cá nhân của người dùng.
 */
public interface ThongBaoDichVu {

    KetQuaPhanTrang<PhanHoiThongBao> layThongBaoCuaToi(Long maNguoiDung, Pageable pageable);

    long demChuaDoc(Long maNguoiDung);

    void danhDauTatCaDaDoc(Long maNguoiDung);

    void danhDauDaDoc(Long maNguoiDung, Long maThongBao);

    /**
     * Điểm vào PUBLIC duy nhất để các module khác (choKhoaHoc, lopHocLMS, thanhToan...)
     * tạo thông báo cho người dùng mà không cần import trực tiếp Entity/Repository nội bộ
     * của module traoDoiTrongLop.
     */
    void guiThongBao(Long maNguoiDung, LoaiThongBao loai, String tieuDe, String noiDung, Map<String, Object> duLieuDong);
}
