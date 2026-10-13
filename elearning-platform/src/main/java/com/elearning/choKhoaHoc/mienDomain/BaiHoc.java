package com.elearning.choKhoaHoc.mienDomain;

import com.elearning.choKhoaHoc.mienDomain.enumDomain.LoaiBaiHoc;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Khớp bảng bai_hoc. duongDan là NOT NULL ở CSDL — với bài học loại QUIZ
 * (không có file), Service sẽ ghi chuỗi rỗng thay vì null để tôn trọng
 * ràng buộc đã có sẵn trong migration (không sửa migration đã chạy).
 */
@Entity
@Table(name = "bai_hoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaiHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_bai_hoc")
    private Long maBaiHoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chuong", nullable = false)
    private Chuong chuong;

    @Column(name = "tieu_de", nullable = false, length = 255)
    private String tieuDe;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false, length = 20)
    private LoaiBaiHoc loai;

    @Column(name = "vi_tri", nullable = false)
    private Integer viTri;

    /** URL Cloudinary (video) hoặc Cloudflare R2 (PDF/DOC). Rỗng "" với bài học QUIZ. */
    @Column(name = "duong_dan", nullable = false, columnDefinition = "TEXT")
    private String duongDan = "";

    @Column(name = "duoi_tep", length = 10)
    private String duoiTep;

    /** Metadata kỹ thuật trích xuất tự động: thời lượng, định dạng, độ phân giải... */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();

    @Column(name = "xem_thu")
    private Boolean xemThu = false;

    public BaiHoc(Chuong chuong, String tieuDe, LoaiBaiHoc loai, Integer viTri) {
        this.chuong = chuong;
        this.tieuDe = tieuDe;
        this.loai = loai;
        this.viTri = viTri;
    }

    /**
     * "Ẩn tạm thời" — cờ nghiệp vụ lưu trong cột metadata JSONB sẵn có (khóa
     * "an") thay vì thêm cột mới, để tôn trọng ràng buộc không đổi schema DB
     * của dự án. Dùng thay cho xóa vật lý khi khóa học đã có học viên sở
     * hữu (xem QuanLyNoiDungDichVuImpl.xoaBaiHoc/anBaiHoc/hienBaiHoc).
     */
    public boolean isAn() {
        return metadata != null && Boolean.TRUE.equals(metadata.get("an"));
    }

    public void datAn(boolean an) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        if (an) {
            metadata.put("an", true);
        } else {
            metadata.remove("an");
        }
    }
}