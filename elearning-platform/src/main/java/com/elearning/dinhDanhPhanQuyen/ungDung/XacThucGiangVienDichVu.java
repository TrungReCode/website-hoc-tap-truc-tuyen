package com.elearning.dinhDanhPhanQuyen.ungDung;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiXacThucGiangVien;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDuyetXacThucGiangVien;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauXacThucGiangVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface XacThucGiangVienDichVu {

    /** Gửi (hoặc gửi lại sau khi bị từ chối) yêu cầu xác thực chuyên môn. */
    PhanHoiXacThucGiangVien guiYeuCau(Long maNguoiDung, YeuCauXacThucGiangVien yeuCau, MultipartFile fileMinhChung);

    /** Tự xem trạng thái xác thực của chính mình — rỗng nếu chưa từng gửi yêu cầu. */
    Optional<PhanHoiXacThucGiangVien> layTrangThaiCuaToi(Long maNguoiDung);

    /** Có được phép tạo khóa học hay không — dùng ở KhoaHocDichVuImpl.taoKhoaHoc. */
    boolean daDuocXacThuc(Long maNguoiDung);

    /** Admin — danh sách theo trạng thái (mặc định CHO_DUYET nếu không truyền). */
    Page<PhanHoiXacThucGiangVien> layDanhSachChoAdmin(String trangThai, Pageable pageable);

    /** Admin — duyệt hoặc từ chối 1 yêu cầu. */
    PhanHoiXacThucGiangVien duyetYeuCau(Long maNguoiDungAdmin, Long maXacThuc, YeuCauDuyetXacThucGiangVien yeuCau);

    /**
     * Admin — tải nội dung file minh chứng gốc (bytes thô + tên file) để
     * xem/tải xuống khi xét duyệt. Tự kiểm tra quyền truy cập bên trong.
     */
    KetQuaTaiFileXacThuc taiFileMinhChung(Long maNguoiDungAdmin, Long maXacThuc);

    record KetQuaTaiFileXacThuc(byte[] noiDung, String tenFileGoc) {
    }
}
