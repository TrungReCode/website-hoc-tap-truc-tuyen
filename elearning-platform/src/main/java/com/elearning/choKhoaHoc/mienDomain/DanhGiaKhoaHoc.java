package com.elearning.choKhoaHoc.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Khớp bảng danh_gia_khoa_hoc. maNguoiDung chỉ lưu BIGINT thuần (không
 * map sang Entity NguoiDung) — cùng lý do với KhoaHoc.maTacGia.
 * UNIQUE(ma_nguoi_dung, ma_khoa_hoc) đã được ràng buộc ở DB; Service vẫn
 * kiểm tra trước qua Redis Lock để tránh race-condition tạo trùng đánh
 * giá khi người dùng bấm gửi nhiều lần liên tiếp.
 */
@Entity
@Table(name = "danh_gia_khoa_hoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DanhGiaKhoaHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_danh_gia")
    private Long maDanhGia;

    @Column(name = "ma_nguoi_dung", nullable = false)
    private Long maNguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khoa_hoc", nullable = false)
    private KhoaHoc khoaHoc;

    @Column(name = "so_sao", nullable = false)
    private Integer soSao;

    @Column(name = "nhan_xet", columnDefinition = "TEXT")
    private String nhanXet;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    /** Ẩn tạm thời khỏi danh sách công khai và khỏi điểm trung bình — do QTV nội dung xử lý vi phạm. */
    @Column(name = "an", nullable = false)
    private Boolean an = false;

    @Column(name = "ly_do_an", columnDefinition = "TEXT")
    private String lyDoAn;

    public DanhGiaKhoaHoc(Long maNguoiDung, KhoaHoc khoaHoc, Integer soSao, String nhanXet) {
        this.maNguoiDung = maNguoiDung;
        this.khoaHoc = khoaHoc;
        this.soSao = soSao;
        this.nhanXet = nhanXet;
        this.ngayTao = LocalDateTime.now();
        this.an = false;
    }

    public void anDoViPham(String lyDo) {
        this.an = true;
        this.lyDoAn = lyDo;
    }

    public void boHienThi() {
        this.an = false;
        this.lyDoAn = null;
    }
}
