package com.elearning.lopHocLMS.ungDung;

import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiCauHoiBoSung;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiMucGiaoTrinhLai;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCapNhatBaiBoSung;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauTaoBaiBoSung;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauTaoCauHoiBoSung;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BaiBoSungDichVu {

    PhanHoiMucGiaoTrinhLai themBaiBoSung(Long maNguoiDungGiangVien, Long maLopHoc, YeuCauTaoBaiBoSung yeuCau, MultipartFile file);

    PhanHoiMucGiaoTrinhLai capNhatBaiBoSung(Long maNguoiDungGiangVien, Long maBaiHocBoSung, YeuCauCapNhatBaiBoSung yeuCau);

    void xoaBaiBoSung(Long maNguoiDungGiangVien, Long maBaiHocBoSung);

    void themCauHoiBoSung(Long maNguoiDungGiangVien, Long maBaiHocBoSung, YeuCauTaoCauHoiBoSung yeuCau);

    void xoaCauHoiBoSung(Long maNguoiDungGiangVien, Long maCauHoiBoSung);

    /** Danh sách câu hỏi (kèm đáp án đúng) của một bài kiểm tra bổ sung — dùng cho giảng viên quản lý. */
    List<PhanHoiCauHoiBoSung> layCauHoiBoSung(Long maNguoiDungGiangVien, Long maBaiHocBoSung);
}