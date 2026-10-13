package com.elearning.choKhoaHoc.ungDung;
 
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhSachKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauCapNhatKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoKhoaHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
 
import java.util.List;
 
/**
 * PUBLIC — interface tầng ứng dụng, là cổng giao tiếp chính của module
 * choKhoaHoc dành cho Controller (UC-03 Xem Chợ khóa học, UC-08 Quản lý
 * khóa học của Giảng viên).
 */
public interface KhoaHocDichVu {
 
    PhanHoiKhoaHoc taoKhoaHoc(Long maNguoiDung, YeuCauTaoKhoaHoc yeuCau);
 
    PhanHoiKhoaHoc capNhatKhoaHoc(Long maNguoiDung, Long maKhoaHoc, YeuCauCapNhatKhoaHoc yeuCau);
 
    void xoaKhoaHoc(Long maNguoiDung, Long maKhoaHoc);
 
    /** Xem chi tiết dành cho tác giả/admin — trả về bất kể trạng thái nào. */
    PhanHoiKhoaHoc layChiTietChoTacGia(Long maNguoiDung, Long maKhoaHoc);
 
    /** Xem chi tiết công khai (UC-03) — chỉ trả về khi trạng thái CONG_KHAI. */
    PhanHoiKhoaHoc layChiTietCongKhai(Long maKhoaHoc);
 
    List<PhanHoiDanhSachKhoaHoc> layKhoaHocCuaToi(Long maNguoiDung);

    /**
     * UC-09 Tạo lớp học — danh sách khóa học mà người dùng ĐƯỢC PHÉP dùng để
     * mở lớp: khóa học do chính mình là tác giả, HOẶC khóa học người khác
     * nhưng mình đã mua Giấy phép Giảng dạy (DAY_HOC) còn hiệu lực. Khác với
     * layKhoaHocCuaToi() (chỉ trả về khóa học tự viết) và layKhoaHocDaMua()
     * (gồm cả khóa học chỉ mua để tự học — CA_NHAN — không đủ quyền mở lớp).
     */
    List<PhanHoiDanhSachKhoaHoc> layKhoaHocCoTheMoLop(Long maNguoiDung);
 
    /** UC học tự học — danh sách khóa học học viên đã mua (quyền CA_NHAN hoặc DAY_HOC còn hiệu lực). */
    List<PhanHoiDanhSachKhoaHoc> layKhoaHocDaMua(Long maNguoiDung);
 
    /**
     * UC học tự học — xem chi tiết đầy đủ (không ẩn nội dung) dành cho học viên
     * đã sở hữu khóa học. Ném NgoaiLeKhongCoQuyen nếu chưa mua.
     */
    PhanHoiKhoaHoc layChiTietChoHocVien(Long maNguoiDung, Long maKhoaHoc);
 
    Page<PhanHoiDanhSachKhoaHoc> timKiemCongKhai(Long maDanhMuc, String tuKhoa, String caNhanLoc, String dayHocLoc, Pageable pageable);
 
    /** NHAP -> CHO_KIEM_DUYET. */
    PhanHoiKhoaHoc guiKiemDuyet(Long maNguoiDung, Long maKhoaHoc);
 
    /** CHO_KIEM_DUYET -> NHAP (giảng viên rút lại trước khi Admin xử lý). */
    PhanHoiKhoaHoc rutLaiKiemDuyet(Long maNguoiDung, Long maKhoaHoc);
 
    /** CONG_KHAI -> LUU_TRU (giảng viên tự ẩn, dữ liệu vẫn giữ nguyên). */
    PhanHoiKhoaHoc luuTru(Long maNguoiDung, Long maKhoaHoc);
 
    /** LUU_TRU -> CONG_KHAI. */
    PhanHoiKhoaHoc taiCongKhai(Long maNguoiDung, Long maKhoaHoc);
}