package com.elearning.choKhoaHoc.ungDung;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiBaiHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiChuong;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauCapNhatBaiHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoBaiHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoCauHoi;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoChuong;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * PUBLIC — quản lý giáo trình (chương/bài học/quiz) của khóa học, dành
 * cho Giảng viên (tác giả) đang soạn thảo nội dung (UC-08), và cũng được
 * DuyetKhoaHocDichVu dùng để hiển thị giáo trình cho Admin xem khi kiểm
 * duyệt (UC-11).
 */
public interface QuanLyNoiDungDichVu {

    Optional<PhanHoiBaiHoc> layChiTietBaiHoc(Long maBaiHoc);

    List<PhanHoiChuong> layGiaoTrinh(Long maKhoaHoc);

    /**
     * Giống layGiaoTrinh nhưng loại bỏ các bài học đang ở trạng thái "ẩn"
     * (xem BaiHoc.isAn()) — dùng cho mọi luồng hiển thị nội dung cho học
     * viên/khách xem (marketplace công khai, học viên đã mua, tính tiến độ
     * hoàn thành). Giảng viên soạn thảo và Admin kiểm duyệt vẫn dùng
     * layGiaoTrinh (đầy đủ, kể cả bài đã ẩn) để có thể quản lý/hiện lại.
     */
    List<PhanHoiChuong> layGiaoTrinhHienThi(Long maKhoaHoc);

    /** Ẩn tạm thời bài học (thay cho xóa cứng khi khóa học đã có học viên sở hữu). */
    PhanHoiBaiHoc anBaiHoc(Long maNguoiDung, Long maBaiHoc);

    /** Hiện lại bài học đã ẩn. */
    PhanHoiBaiHoc hienBaiHoc(Long maNguoiDung, Long maBaiHoc);

    PhanHoiChuong themChuong(Long maNguoiDung, Long maKhoaHoc, YeuCauTaoChuong yeuCau);

    PhanHoiChuong capNhatChuong(Long maNguoiDung, Long maChuong, YeuCauTaoChuong yeuCau);

    void xoaChuong(Long maNguoiDung, Long maChuong);

    /**
     * Thêm bài học. file là bắt buộc khi yeuCau.loai() là VIDEO/PDF/DOC,
     * bỏ qua (null) khi loai() = QUIZ (Service sẽ tự tạo kèm BaiKiemTra rỗng).
     */
    PhanHoiBaiHoc themBaiHoc(Long maNguoiDung, Long maChuong, YeuCauTaoBaiHoc yeuCau, MultipartFile file);

    PhanHoiBaiHoc capNhatBaiHoc(Long maNguoiDung, Long maBaiHoc, YeuCauCapNhatBaiHoc yeuCau);

    void xoaBaiHoc(Long maNguoiDung, Long maBaiHoc);

    PhanHoiBaiHoc themCauHoi(Long maNguoiDung, Long maBaiHoc, YeuCauTaoCauHoi yeuCau);

    void xoaCauHoi(Long maNguoiDung, Long maCauHoi);
}
