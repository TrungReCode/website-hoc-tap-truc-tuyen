package com.elearning.choKhoaHoc.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bai_kiem_tra")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaiKiemTra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_bai_kiem_tra")
    private Long maBaiKiemTra;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_hoc", nullable = false, unique = true)
    private BaiHoc baiHoc;

    @Column(name = "diem_toi_thieu", nullable = false)
    private Integer diemToiThieu = 70;

    /** Thời gian làm bài tính bằng phút; NULL nghĩa là không giới hạn thời gian. */
    @Column(name = "thoi_gian")
    private Integer thoiGian;

    @OneToMany(mappedBy = "baiKiemTra", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("viTri ASC")
    private List<CauHoi> danhSachCauHoi = new ArrayList<>();

    public BaiKiemTra(BaiHoc baiHoc, Integer diemToiThieu, Integer thoiGian) {
        this.baiHoc = baiHoc;
        this.diemToiThieu = diemToiThieu != null ? diemToiThieu : 70;
        this.thoiGian = thoiGian;
    }
}
