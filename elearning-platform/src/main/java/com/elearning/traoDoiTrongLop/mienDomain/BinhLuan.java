package com.elearning.traoDoiTrongLop.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Khớp bảng binh_luan — bình luận cây phân cấp (1 cấp reply).
 * maCha NULL = bình luận gốc; maCha != NULL = reply của bình luận gốc.
 * Thiết kế chỉ hỗ trợ 1 cấp lồng nhau để đơn giản hoá frontend;
 * không cho phép reply lồng trong reply.
 */
@Entity
@Table(name = "binh_luan")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinhLuan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_binh_luan")
    private Long maBinhLuan;

    @Column(name = "ma_bai_dang", nullable = false)
    private Long maBaiDang;

    @Column(name = "ma_nguoi_dung", nullable = false)
    private Long maNguoiDung;

    /** NULL = bình luận gốc, != NULL = reply. */
    @Column(name = "ma_cha")
    private Long maCha;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    public BinhLuan(Long maBaiDang, Long maNguoiDung, Long maCha, String noiDung) {
        this.maBaiDang   = maBaiDang;
        this.maNguoiDung = maNguoiDung;
        this.maCha       = maCha;
        this.noiDung     = noiDung;
        this.ngayTao     = LocalDateTime.now();
    }

    public boolean laChuSoHuu(Long maNguoiDung) {
        return this.maNguoiDung.equals(maNguoiDung);
    }

    public boolean laBinhLuanGoc() {
        return this.maCha == null;
    }
}
