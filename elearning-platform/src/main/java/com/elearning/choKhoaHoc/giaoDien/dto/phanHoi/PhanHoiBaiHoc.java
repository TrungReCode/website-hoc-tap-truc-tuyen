package com.elearning.choKhoaHoc.giaoDien.dto.phanHoi;

import com.elearning.choKhoaHoc.mienDomain.enumDomain.LoaiBaiHoc;

import java.util.Map;

public record PhanHoiBaiHoc(
        Long maBaiHoc,
        String tieuDe,
        LoaiBaiHoc loai,
        Integer viTri,
        String duongDan,
        String duoiTep,
        Map<String, Object> metadata,
        boolean xemThu,
        PhanHoiBaiKiemTra baiKiemTra, // null nếu loai != QUIZ
        /** null nếu không truy vấn theo học viên cụ thể (vd. giảng viên quản lý nội dung) —
         *  chỉ được điền khi gọi từ layChiTietChoHocVien (xem KhoaHocDichVuImpl). */
        Boolean hoanThanh,
        /** Vị trí dừng (giây với VIDEO, số trang với PDF/DOC) — null nếu chưa từng xem. */
        Long viTriDung
) {
    /** Constructor tiện dụng cho các ngữ cảnh KHÔNG cần tiến độ (giữ tương thích ngược,
     *  tránh phải sửa mọi nơi khác đang new PhanHoiBaiHoc(...) với 9 tham số cũ). */
    public PhanHoiBaiHoc(Long maBaiHoc, String tieuDe, LoaiBaiHoc loai, Integer viTri, String duongDan,
                          String duoiTep, Map<String, Object> metadata, boolean xemThu, PhanHoiBaiKiemTra baiKiemTra) {
        this(maBaiHoc, tieuDe, loai, viTri, duongDan, duoiTep, metadata, xemThu, baiKiemTra, null, null);
    }
}
