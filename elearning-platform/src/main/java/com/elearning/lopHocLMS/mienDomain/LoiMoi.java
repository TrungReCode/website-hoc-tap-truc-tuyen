package com.elearning.lopHocLMS.mienDomain;

import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLoiMoi;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "loi_moi")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoiMoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_loi_moi")
    private Long maLoiMoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_lop_hoc", nullable = false)
    private LopHoc lopHoc;

    @Column(name = "email_hocvien", nullable = false, length = 100)
    private String emailHocVien;

    @Column(name = "ma_nguoi_moi", nullable = false)
    private Long maNguoiMoi;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "trang_thai", nullable = false, columnDefinition = "trang_thai_loi_moi_enum")
    private TrangThaiLoiMoi trangThai = TrangThaiLoiMoi.CHO_XAC_NHAN;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_het_han", nullable = false)
    private LocalDateTime ngayHetHan;

    public LoiMoi(LopHoc lopHoc, String emailHocVien, Long maNguoiMoi, LocalDateTime ngayHetHan) {
        this.lopHoc = lopHoc;
        this.emailHocVien = emailHocVien;
        this.maNguoiMoi = maNguoiMoi;
        this.trangThai = TrangThaiLoiMoi.CHO_XAC_NHAN;
        this.ngayTao = LocalDateTime.now();
        this.ngayHetHan = ngayHetHan;
    }

    public boolean conHan() {
        return ngayHetHan.isAfter(LocalDateTime.now());
    }
}
