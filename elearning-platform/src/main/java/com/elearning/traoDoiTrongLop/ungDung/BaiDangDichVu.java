package com.elearning.traoDoiTrongLop.ungDung;

import com.elearning.dungChung.phanTrang.KetQuaPhanTrang;
import com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi.PhanHoiBaiDang;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauCapNhatBaiDang;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauTaoBaiDang;
import com.elearning.traoDoiTrongLop.mienDomain.TepDinhKem;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiBaiDang;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * PUBLIC — UC trao đổi lớp học: CRUD bài đăng diễn đàn.
 * Chỉ thành viên hoặc giảng viên của lớp mới có quyền đọc/viết.
 * Bài loại THONG_BAO chỉ giảng viên được tạo.
 */
public interface BaiDangDichVu {

    PhanHoiBaiDang taoBaiDang(Long maNguoiDung, Long maLopHoc, YeuCauTaoBaiDang yeuCau);

    PhanHoiBaiDang capNhatBaiDang(Long maNguoiDung, Long maBaiDang, YeuCauCapNhatBaiDang yeuCau);

    void xoaBaiDang(Long maNguoiDung, Long maBaiDang);

    /** Giảng viên đánh dấu bài HOI_DAP là đã được giải đáp. */
    PhanHoiBaiDang danhDauGiaiDap(Long maNguoiDungGiangVien, Long maBaiDang, boolean giaiDap);

    PhanHoiBaiDang layChiTiet(Long maNguoiDung, Long maBaiDang);

    KetQuaPhanTrang<PhanHoiBaiDang> layDanhSach(Long maNguoiDung, Long maLopHoc,
                                                 LoaiBaiDang loai, Pageable pageable);

    /**
     * UC-09/UC-10 — tải lên 1 file đính kèm cho bài đăng (bài tập/tài liệu thực hành).
     * Chỉ thành viên hoặc giảng viên của lớp mới upload được. Trả về value object
     * để frontend gom vào danh sách tepDinhKem khi tạo/sửa bài đăng.
     */
    TepDinhKem taiLenTepDinhKem(Long maNguoiDung, Long maLopHoc, MultipartFile file);
}