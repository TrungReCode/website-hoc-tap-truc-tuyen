package com.elearning.dinhDanhPhanQuyen.mienDomain;

import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.TrangThaiXacThucGiangVien;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Khớp bảng xac_thuc_giang_vien (V16). 1 người dùng — 1 bản ghi (UNIQUE
 * ma_nguoi_dung) — gửi lại sau khi bị từ chối sẽ cập nhật lại chính bản ghi
 * này (xem XacThucGiangVienDichVuImpl.guiYeuCau), không tạo bản ghi mới.
 *
 * trang_thai là VARCHAR(20) thường (không phải Postgres native enum) —
 * @Enumerated(EnumType.STRING) ở đây an toàn tuyệt đối, không dính lớp
 * phức tạp CAST/@JdbcTypeCode vốn chỉ cần cho native enum.
 */
@Entity
@Table(name = "xac_thuc_giang_vien")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class XacThucGiangVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_xac_thuc")
    private Long maXacThuc;

    @Column(name = "ma_nguoi_dung", nullable = false, unique = true)
    private Long maNguoiDung;

    @Column(name = "ngay_sinh", nullable = false)
    private LocalDate ngaySinh;

    @Column(name = "bang_cap", nullable = false, columnDefinition = "TEXT")
    private String bangCap;

    @Column(name = "duong_dan_luu_tru", nullable = false, columnDefinition = "TEXT")
    private String duongDanLuuTru;

    @Column(name = "ten_file_goc")
    private String tenFileGoc;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private TrangThaiXacThucGiangVien trangThai = TrangThaiXacThucGiangVien.CHO_DUYET;

    @Column(name = "ly_do_tu_choi", columnDefinition = "TEXT")
    private String lyDoTuChoi;

    @Column(name = "ma_admin_xu_ly")
    private Long maAdminXuLy;

    @Column(name = "ngay_gui", nullable = false)
    private LocalDateTime ngayGui = LocalDateTime.now();

    @Column(name = "ngay_xu_ly")
    private LocalDateTime ngayXuLy;

    public XacThucGiangVien(Long maNguoiDung, LocalDate ngaySinh, String bangCap,
            String duongDanLuuTru, String tenFileGoc) {
        this.maNguoiDung = maNguoiDung;
        this.ngaySinh = ngaySinh;
        this.bangCap = bangCap;
        this.duongDanLuuTru = duongDanLuuTru;
        this.tenFileGoc = tenFileGoc;
        this.trangThai = TrangThaiXacThucGiangVien.CHO_DUYET;
        this.ngayGui = LocalDateTime.now();
        this.lyDoTuChoi = null;
        this.maAdminXuLy = null;
        this.ngayXuLy = null;
    }

    /** Cập nhật lại để gửi lại yêu cầu mới sau khi đã bị từ chối trước đó — KHÔNG tạo bản ghi mới. */
    public void guiLai(LocalDate ngaySinh, String bangCap, String duongDanLuuTru, String tenFileGoc) {
        this.ngaySinh = ngaySinh;
        this.bangCap = bangCap;
        this.duongDanLuuTru = duongDanLuuTru;
        this.tenFileGoc = tenFileGoc;
        this.trangThai = TrangThaiXacThucGiangVien.CHO_DUYET;
        this.ngayGui = LocalDateTime.now();
        this.lyDoTuChoi = null;
        this.maAdminXuLy = null;
        this.ngayXuLy = null;
    }

    public boolean duDieuKienDoTuoi() {
        return ngaySinh != null && !ngaySinh.plusYears(18).isAfter(LocalDate.now());
    }
}
