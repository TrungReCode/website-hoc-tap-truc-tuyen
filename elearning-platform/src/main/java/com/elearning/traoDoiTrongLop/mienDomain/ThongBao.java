package com.elearning.traoDoiTrongLop.mienDomain;

import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Khớp bảng thong_bao — thông báo gửi tới từng người dùng.
 * duLieuDong (JSONB) chứa dữ liệu tuỳ loại thông báo:
 *   BINH_LUAN_MOI → {maBaiDang, maBinhLuan, maLopHoc, tieuDeBaiDang}
 *   BAI_DANG_MOI  → {maBaiDang, maLopHoc, tieuDeBaiDang}
 *   GIAI_DAP      → {maBaiDang, maLopHoc, tieuDeBaiDang}
 *
 * thongBaoTuongTac = true đánh dấu các thông báo mà người dùng CẦN làm gì đó
 * tiếp theo (sửa khóa học bị từ chối/yêu cầu bổ sung, tài khoản bị khóa...),
 * khác với thông báo chỉ mang tính thông tin (có bài đăng mới, thanh toán
 * thành công...). Frontend dùng cờ này để gắn nhãn "Cần xử lý".
 */
@Entity
@Table(name = "thong_bao")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThongBao {

    // Các loại thông báo mà người nhận cần thực hiện thêm một hành động,
    // khác với thông báo chỉ mang tính thông tin đơn thuần.
    private static final java.util.Set<LoaiThongBao> LOAI_CAN_HANH_DONG = java.util.EnumSet.of(
            LoaiThongBao.KHOA_HOC_BI_TU_CHOI,
            LoaiThongBao.KHOA_HOC_YEU_CAU_BO_SUNG,
            LoaiThongBao.HOAN_TIEN_TU_CHOI,
            LoaiThongBao.TAI_KHOAN_BI_KHOA
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_thong_bao")
    private Long maThongBao;

    @Column(name = "ma_nguoi_dung", nullable = false)
    private Long maNguoiDung;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false, length = 50)
    private LoaiThongBao loai;

    @Column(name = "tieu_de", nullable = false)
    private String tieuDe;

    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "trang_thai_doc")
    private Boolean trangThaiDoc = false;

    @Column(name = "thong_bao_tuong_tac")
    private Boolean thongBaoTuongTac = false;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "du_lieu_dong", columnDefinition = "jsonb")
    private Map<String, Object> duLieuDong = new HashMap<>();

    public ThongBao(Long maNguoiDung, LoaiThongBao loai, String tieuDe,
                    String noiDung, Map<String, Object> duLieuDong) {
        this.maNguoiDung    = maNguoiDung;
        this.loai           = loai;
        this.tieuDe         = tieuDe;
        this.noiDung        = noiDung;
        this.trangThaiDoc   = false;
        this.thongBaoTuongTac = LOAI_CAN_HANH_DONG.contains(loai);
        this.ngayTao        = LocalDateTime.now();
        this.duLieuDong     = duLieuDong != null ? duLieuDong : new HashMap<>();
    }

    public void danhDauDaDoc() {
        this.trangThaiDoc = true;
    }
}