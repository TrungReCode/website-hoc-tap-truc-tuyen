package com.elearning.lopHocLMS.ungDung;

import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiDeKiemTra;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiDeKiemTraChoAdmin;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiKetQuaQuiz;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauNopBaiKiemTra;

import java.util.Optional;

public interface KiemTraTrongLopDichVu {

    PhanHoiDeKiemTra layDeBaiGoc(Long maNguoiDung, Long maLopHoc, Long maBaiHoc);

    /** Lấy đề bài kiểm tra gốc khi TỰ HỌC (không qua lớp) — chỉ cần đã sở hữu khóa học. */
    PhanHoiDeKiemTra layDeBaiTuHoc(Long maNguoiDung, Long maBaiHoc);

    PhanHoiDeKiemTra layDeBoSung(Long maNguoiDung, Long maLopHoc, Long maBaiHocBoSung);

    PhanHoiKetQuaQuiz nopBaiGoc(Long maNguoiDung, Long maLopHoc, Long maBaiHoc, YeuCauNopBaiKiemTra yeuCau);

    /** Nộp bài kiểm tra gốc khi TỰ HỌC — lưu kết quả với maLopHoc = NULL (xem V15). */
    PhanHoiKetQuaQuiz nopBaiTuHoc(Long maNguoiDung, Long maBaiHoc, YeuCauNopBaiKiemTra yeuCau);

    PhanHoiKetQuaQuiz nopBaiBoSung(Long maNguoiDung, Long maLopHoc, Long maBaiHocBoSung, YeuCauNopBaiKiemTra yeuCau);

    Optional<PhanHoiKetQuaQuiz> layKetQuaGanNhatGoc(Long maNguoiDung, Long maBaiHoc);

    Optional<PhanHoiKetQuaQuiz> layKetQuaGanNhatBoSung(Long maNguoiDung, Long maBaiHocBoSung);

    /**
     * Cho Quản trị viên nội dung xem đầy đủ câu hỏi + đáp án đúng của 1 bài
     * Quiz (loaiNguon: "GOC" | "BO_SUNG") để kiểm duyệt — KHÔNG kiểm tra
     * thành viên lớp, vì mục đích là giám sát nội dung, không phải làm bài.
     */
    PhanHoiDeKiemTraChoAdmin layDeBaiChoAdmin(String loaiNguon, Long maThamChieu);
}