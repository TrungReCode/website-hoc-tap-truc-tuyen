package com.elearning.choKhoaHoc.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Khớp bảng danh_muc (V2__tao_bang_khoa_hoc.sql). Hỗ trợ phân cấp 2 mức
 * (danh mục cha → danh mục con) qua tự tham chiếu mucCha.
 *
 * PUBLIC: lý do kỹ thuật giống NguoiDung — Repository (package haTang)
 * khác package với Entity (package mienDomain) nên buộc phải public.
 */
@Entity
@Table(name = "danh_muc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_danh_muc")
    private Long maDanhMuc;

    @Column(name = "ten_danh_muc", nullable = false, unique = true, length = 100)
    private String tenDanhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "muc_cha")
    private DanhMuc mucCha;

    public DanhMuc(String tenDanhMuc, DanhMuc mucCha) {
        this.tenDanhMuc = tenDanhMuc;
        this.mucCha = mucCha;
    }
}
