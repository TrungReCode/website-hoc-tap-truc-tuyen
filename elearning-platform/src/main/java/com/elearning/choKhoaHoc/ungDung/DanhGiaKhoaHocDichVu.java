package com.elearning.choKhoaHoc.ungDung;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhGia;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauDanhGia;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauXuLyDanhGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * PUBLIC — UC-15 Đánh giá khóa học.
 *
 * Điều kiện tiền đề: người dùng phải đã sở hữu khóa học (bảng quyen_su_dung,
 * do lopHocLMS quản lý) — được kiểm tra tại DanhGiaKhoaHocDichVuImpl.taoDanhGia
 * qua QuyenSuDungKho.existsByMaNguoiDungAndMaKhoaHoc. Nếu sở hữu qua một lớp
 * học dựa trên khóa học này, còn yêu cầu thêm tiến độ tối thiểu trong lớp đó
 * (qua GhiDanhDichVu.layLopCuaHocVien + PhanHoiLop.tiLeHoanThanh).
 */
public interface DanhGiaKhoaHocDichVu {

    PhanHoiDanhGia taoDanhGia(Long maNguoiDung, Long maKhoaHoc, YeuCauDanhGia yeuCau);

    PhanHoiDanhGia capNhatDanhGia(Long maNguoiDung, Long maDanhGia, YeuCauDanhGia yeuCau);

    void xoaDanhGia(Long maNguoiDung, Long maDanhGia);

    Page<PhanHoiDanhGia> layDanhGiaTheoKhoaHoc(Long maKhoaHoc, Pageable pageable);

    Optional<PhanHoiDanhGia> layDanhGiaCuaToi(Long maNguoiDung, Long maKhoaHoc);

    // ──────────────── UC-11: Kiểm duyệt đánh giá vi phạm (Quản trị viên nội dung) ────────────────

    /** Ẩn tạm thời một đánh giá vi phạm tiêu chuẩn cộng đồng — giữ bản ghi, loại khỏi danh sách công khai. */
    PhanHoiDanhGia anDanhGiaViPham(Long maNguoiDungAdmin, Long maDanhGia, YeuCauXuLyDanhGia yeuCau);

    /** Xóa hẳn một đánh giá vi phạm nghiêm trọng. */
    void xoaDanhGiaViPham(Long maNguoiDungAdmin, Long maDanhGia, YeuCauXuLyDanhGia yeuCau);

    /** Danh sách ĐẦY ĐỦ (kể cả đã ẩn) cho màn hình kiểm duyệt của Admin — khác với bản công khai đã lọc. */
    Page<PhanHoiDanhGia> layTatCaDanhGiaChoAdmin(Long maKhoaHoc, Pageable pageable);

    /** Bỏ ẩn một đánh giá trước đó đã bị xử lý nhầm hoặc không còn vi phạm. */
    PhanHoiDanhGia boAnDanhGia(Long maNguoiDungAdmin, Long maDanhGia);
}