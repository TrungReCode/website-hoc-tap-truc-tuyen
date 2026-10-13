package com.elearning.dinhDanhPhanQuyen.mienDomain;

import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.TrangThaiTaiKhoan;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.VaiTro;
import com.elearning.dungChung.baoMat.NguoiDungThamChieu;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Khớp với bảng nguoi_dung trong V1__tao_bang_nguoi_dung.sql.
 *
 * Entity giữ PUBLIC (khác với Repository/Service luôn package-private):
 * lý do kỹ thuật là Hibernate proxy và Repository ở package "haTang"
 * (khác package với "mienDomain") cần truy cập được Entity này khi biên
 * dịch. Ranh giới module vẫn được giữ nguyên vì module NGOÀI
 * dinhDanhPhanQuyen không có cách nào lấy được instance NguoiDung —
 * NguoiDungKho (nơi duy nhất query ra Entity) là package-private, nên
 * không ai "vô tình" import được Repository để lấy Entity ra ngoài.
 *
 * Implements UserDetails để Spring Security dùng trực tiếp làm Principal,
 * tránh phải viết thêm class wrapper trung gian.
 */
@Entity
@Table(name = "nguoi_dung")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA yêu cầu constructor rỗng
public class NguoiDung implements UserDetails, NguoiDungThamChieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_nguoi_dung")
    private Long maNguoiDung;

    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "sdt", length = 15)
    private String sdt;

    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "vai_tro", nullable = false, columnDefinition = "vai_tro_enum")
    private VaiTro vaiTro;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "trang_thai", nullable = false, columnDefinition = "trang_thai_nd_enum")
    private TrangThaiTaiKhoan trangThai;

    @Column(name = "anh_dai_dien")
    private String anhDaiDien;

    @Column(name = "tieu_su")
    private String tieuSu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    public NguoiDung(String hoTen, String email, String matKhau) {
        this(hoTen, email, matKhau, null);
    }

    public NguoiDung(String hoTen, String email, String matKhau, String sdt) {
        this.hoTen = hoTen;
        this.email = email;
        this.matKhau = matKhau;
        this.sdt = sdt;
        this.vaiTro = VaiTro.NGUOI_DUNG;
        this.trangThai = TrangThaiTaiKhoan.CHO_XAC_NHAN;
        this.ngayTao = LocalDateTime.now();
    }

    // ===== Triển khai UserDetails =====

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + vaiTro.name()));
    }

    @Override
    public String getPassword() {
        return matKhau;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return trangThai != TrangThaiTaiKhoan.BI_CAM;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return trangThai == TrangThaiTaiKhoan.HOAT_DONG;
    }
}
