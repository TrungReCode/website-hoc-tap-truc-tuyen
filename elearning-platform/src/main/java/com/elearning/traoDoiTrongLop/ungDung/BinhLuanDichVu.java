package com.elearning.traoDoiTrongLop.ungDung;

import com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi.PhanHoiBinhLuan;
import com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi.PhanHoiTrangThaiNopBai;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauChamDiem;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauNopBaiTap;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauTaoBinhLuan;

import java.util.List;

/**
 * PUBLIC — UC trao đổi lớp học: CRUD bình luận (cây phân cấp 1 cấp), và —
 * dành riêng cho bài đăng loại BAI_TAP — nộp bài/chấm điểm kiểu Google
 * Classroom, dùng chính cơ chế bình luận nhưng KHÔNG cần thêm bảng/cột CSDL
 * (xem ghi chú trong BinhLuanDichVuImpl).
 */
public interface BinhLuanDichVu {

    PhanHoiBinhLuan taoBinhLuan(Long maNguoiDung, Long maBaiDang, YeuCauTaoBinhLuan yeuCau);

    void xoaBinhLuan(Long maNguoiDung, Long maBinhLuan);

    /** Trả về danh sách bình luận gốc kèm replies lồng bên trong. */
    List<PhanHoiBinhLuan> layBinhLuanTheoBAiDang(Long maNguoiDung, Long maBaiDang);

    /**
     * Học viên nộp bài tập cho một bài đăng loại BAI_TAP. Nộp lại (trước khi
     * được chấm hay bất cứ lúc nào) sẽ ghi đè lên bài nộp cũ của chính học
     * viên đó — mỗi học viên chỉ có một bài nộp cho mỗi bài tập.
     */
    PhanHoiBinhLuan nopBaiTap(Long maNguoiDungHocVien, Long maBaiDang, YeuCauNopBaiTap yeuCau);

    /**
     * Giảng viên chấm điểm một bài nộp cụ thể (xác định bằng mã bình luận
     * của chính bài nộp đó). Chấm lại sẽ ghi đè điểm/nhận xét cũ.
     */
    PhanHoiBinhLuan chamDiem(Long maNguoiDungGiangVien, Long maBinhLuanBaiNop, YeuCauChamDiem yeuCau);

    /**
     * Giảng viên xem trạng thái nộp bài của TOÀN BỘ học viên trong lớp cho
     * một bài đăng loại BAI_TAP: đối chiếu danh sách thành viên lớp (đã có
     * sẵn ở module lopHocLMS) với các bình luận NOP_BAI/CHAM_DIEM hiện có ở
     * bảng binh_luan — không cần thêm bảng/cột CSDL mới.
     */
    List<PhanHoiTrangThaiNopBai> layDanhSachNopBai(Long maNguoiDungGiangVien, Long maBaiDang);
}
