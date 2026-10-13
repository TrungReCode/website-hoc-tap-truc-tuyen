package com.elearning.choKhoaHoc.ungDung;

import java.util.List;
import java.util.Optional;

/**
 * PUBLIC — interface dành riêng cho các module nghiệp vụ KHÁC (chủ yếu
 * lopHocLMS) gọi vào, không phải cho Controller của chính choKhoaHoc.
 * Chỉ trả về DTO/giá trị nguyên thủy, tuyệt đối không trả Entity KhoaHoc.
 */
public interface TimKiemKhoaHocDichVu {

    boolean laTacGiaKhoaHoc(Long maNguoiDung, Long maKhoaHoc);

    boolean khoaHocDangCongKhai(Long maKhoaHoc);

    Optional<ThongTinKhoaHocCoBan> layThongTinCoBan(Long maKhoaHoc);

    /**
     * Tìm mã các khóa học đang ở trạng thái CÔNG_KHAI có tiêu đề khớp từ
     * khóa (không phân biệt hoa/thường, khớp một phần) — dùng bởi
     * lopHocLMS để tìm lớp học theo tên khóa học thay vì mã lớp (mã lớp là
     * chuỗi ngẫu nhiên 8 ký tự, học viên chưa tham gia lớp sẽ không biết
     * để tìm kiếm).
     */
    List<Long> timMaKhoaHocKhopTuKhoa(String tuKhoa);
}