package com.elearning.traoDoiTrongLop.mienDomain;

import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiBaiDang;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Khớp bảng bai_dang — bài đăng trong diễn đàn của một lớp học.
 * maLopHoc và maNguoiDang chỉ lưu BIGINT thuần, KHÔNG map sang Entity
 * xuyên module (LopHoc, NguoiDung), đúng quy ước kiến trúc.
 *
 * tepDinhKem lưu JSONB dạng [{ten, duongDan, loai}] để hỗ trợ
 * nhiều file đính kèm mà không cần bảng phụ.
 */
@Entity
@Table(name = "bai_dang")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaiDang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_bai_dang")
    private Long maBaiDang;

    @Column(name = "ma_lop_hoc", nullable = false)
    private Long maLopHoc;

    @Column(name = "ma_nguoi_dang", nullable = false)
    private Long maNguoiDang;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false, length = 20)
    private LoaiBaiDang loai;

    @Column(name = "tieu_de", nullable = false)
    private String tieuDe;

    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String noiDung;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tep_dinh_kem", columnDefinition = "jsonb")
    private List<TepDinhKem> tepDinhKem = new ArrayList<>();

    /** true khi giảng viên đánh dấu bài đã được giải đáp (chỉ áp dụng loại HOI_DAP). */
    @Column(name = "giai_dap")
    private Boolean giaiDap = false;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "xoa_mem")
    private Boolean xoaMem = false;

    public BaiDang(Long maLopHoc, Long maNguoiDang, LoaiBaiDang loai,
                   String tieuDe, String noiDung, List<TepDinhKem> tepDinhKem) {
        this.maLopHoc   = maLopHoc;
        this.maNguoiDang = maNguoiDang;
        this.loai        = loai;
        this.tieuDe      = tieuDe;
        this.noiDung     = noiDung;
        this.tepDinhKem  = tepDinhKem != null ? tepDinhKem : new ArrayList<>();
        this.giaiDap     = false;
        this.xoaMem      = false;
        this.ngayTao     = LocalDateTime.now();
        this.ngayCapNhat = LocalDateTime.now();
    }

    public void capNhat(String tieuDe, String noiDung, List<TepDinhKem> tepDinhKem) {
        this.tieuDe      = tieuDe;
        this.noiDung     = noiDung;
        this.tepDinhKem  = tepDinhKem != null ? tepDinhKem : new ArrayList<>();
        this.ngayCapNhat = LocalDateTime.now();
    }

    public void danhDauGiaiDap(boolean giaiDap) {
        this.giaiDap = giaiDap;
    }

    public void xoaMem() {
        this.xoaMem      = true;
        this.ngayCapNhat = LocalDateTime.now();
    }

    public boolean laChuSoHuu(Long maNguoiDung) {
        return this.maNguoiDang.equals(maNguoiDung);
    }
}
