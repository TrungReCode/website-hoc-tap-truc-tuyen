package com.elearning.choKhoaHoc.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chuong")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chuong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_chuong")
    private Long maChuong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khoa_hoc", nullable = false)
    private KhoaHoc khoaHoc;

    @Column(name = "tieu_de", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "vi_tri", nullable = false)
    private Integer viTri;

    @OneToMany(mappedBy = "chuong", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("viTri ASC")
    private List<BaiHoc> danhSachBaiHoc = new ArrayList<>();

    public Chuong(KhoaHoc khoaHoc, String tieuDe, Integer viTri) {
        this.khoaHoc = khoaHoc;
        this.tieuDe = tieuDe;
        this.viTri = viTri;
    }
}
