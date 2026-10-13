package com.elearning.choKhoaHoc.mienDomain;

import com.elearning.choKhoaHoc.mienDomain.enumDomain.TrangThaiKhoaHoc;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Khớp bảng khoa_hoc. Trường ma_tac_gia chỉ lưu BIGINT (FK thuần, không
 * map @ManyToOne sang NguoiDung) — đúng quy ước: Entity của choKhoaHoc
 * KHÔNG được phép tham chiếu Entity của module dinhDanhPhanQuyen, vì
 * NguoiDung là package-private đối với module ngoài.
 *
 * "Giảng viên" không phải vai trò hệ thống lưu sẵn (vai_tro chỉ có
 * QTV_CAO_CAP/QTV_NOI_DUNG/QTV_TAI_CHINH/NGUOI_DUNG) — bất kỳ NGUOI_DUNG
 * nào tạo khóa học đều đương nhiên trở thành "tác giả" của khóa học đó.
 * Quyền chỉnh sửa được kiểm tra ở tầng Service bằng so khớp maTacGia với
 * mã người dùng đang đăng nhập, không dựa vào vai trò.
 */
@Entity
@Table(name = "khoa_hoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KhoaHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_khoa_hoc")
    private Long maKhoaHoc;

    @Column(name = "ma_tac_gia", nullable = false)
    private Long maTacGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danh_muc", nullable = false)
    private DanhMuc danhMuc;

    @Column(name = "tieu_de", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "mieu_ta", columnDefinition = "TEXT")
    private String mieuTa;

    @Column(name = "anh_bia", columnDefinition = "TEXT")
    private String anhBia;

    @Column(name = "gia", nullable = false, precision = 15, scale = 2)
    private BigDecimal gia = BigDecimal.ZERO;

    /** Giá Giấy phép Giảng dạy; NULL nghĩa là tác giả không mở bán license giảng dạy cho khóa học này. */
    @Column(name = "phi_day_hoc", precision = 15, scale = 2)
    private BigDecimal phiDayHoc;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiKhoaHoc trangThai = TrangThaiKhoaHoc.NHAP;

    @Column(name = "ly_do_tu_choi", columnDefinition = "TEXT")
    private String lyDoTuChoi;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @OneToMany(mappedBy = "khoaHoc", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("viTri ASC")
    private List<Chuong> danhSachChuong = new ArrayList<>();

    public KhoaHoc(Long maTacGia, DanhMuc danhMuc, String tieuDe, String mieuTa) {
        this.maTacGia = maTacGia;
        this.danhMuc = danhMuc;
        this.tieuDe = tieuDe;
        this.mieuTa = mieuTa;
        this.trangThai = TrangThaiKhoaHoc.NHAP;
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = LocalDateTime.now();
    }

    public void danhDauCapNhat() {
        this.ngayCapNhat = LocalDateTime.now();
    }

    public boolean laTacGia(Long maNguoiDung) {
        return this.maTacGia.equals(maNguoiDung);
    }
}