package com.elearning.dinhDanhPhanQuyen.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Khớp với bảng refresh_tokens trong V1__tao_bang_nguoi_dung.sql.
 * Entity PUBLIC (lý do kỹ thuật giống NguoiDung — xem chú thích ở đó),
 * nhưng chỉ NguoiDungKho/MaLamMoiPhienKho và XacThucDichVuImpl (đều
 * package-private) mới thực sự truy cập, nên ranh giới module vẫn giữ.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaLamMoiPhien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Long ma;

    @Column(name = "nguoi_dung_id", nullable = false)
    private Long nguoiDungId;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "het_han", nullable = false)
    private LocalDateTime hetHan;

    @Column(name = "thu_hoi")
    private Boolean thuHoi = false;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    public MaLamMoiPhien(Long nguoiDungId, String token, LocalDateTime hetHan) {
        this.nguoiDungId = nguoiDungId;
        this.token = token;
        this.hetHan = hetHan;
        this.thuHoi = false;
    }

    public boolean conHieuLuc() {
        return !Boolean.TRUE.equals(thuHoi) && hetHan.isAfter(LocalDateTime.now());
    }
}
