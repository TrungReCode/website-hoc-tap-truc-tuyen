package com.elearning.lopHocLMS.ungDung.impl;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiBaiHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiChuong;
import com.elearning.choKhoaHoc.ungDung.QuanLyNoiDungDichVu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiMucGiaoTrinhLai;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCauHinhBaiGoc;
import com.elearning.lopHocLMS.haTang.BaiHocBoSungKho;
import com.elearning.lopHocLMS.haTang.CauHinhBaiGocKho;
import com.elearning.lopHocLMS.haTang.LopHocKho;
import com.elearning.lopHocLMS.haTang.ThanhVienLopKho;
import com.elearning.lopHocLMS.haTang.TienDoBaiBoSungKho;
import com.elearning.lopHocLMS.haTang.TienDoBaiHocKho;
import com.elearning.lopHocLMS.mienDomain.BaiHocBoSung;
import com.elearning.lopHocLMS.mienDomain.CauHinhBaiGoc;
import com.elearning.lopHocLMS.mienDomain.LopHoc;
import com.elearning.lopHocLMS.mienDomain.TienDoBaiBoSung;
import com.elearning.lopHocLMS.mienDomain.TienDoBaiHoc;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;
import com.elearning.lopHocLMS.ungDung.GiaoTrinhLaiDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
class GiaoTrinhLaiDichVuImpl implements GiaoTrinhLaiDichVu {

    private final LopHocKho lopHocKho;
    private final CauHinhBaiGocKho cauHinhBaiGocKho;
    private final BaiHocBoSungKho baiHocBoSungKho;
    private final TienDoBaiHocKho tienDoBaiHocKho;
    private final TienDoBaiBoSungKho tienDoBaiBoSungKho;
    private final ThanhVienLopKho thanhVienLopKho;
    private final QuanLyNoiDungDichVu quanLyNoiDungDichVu; // choKhoaHoc

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiMucGiaoTrinhLai> layGiaoTrinhLaiChoGiangVien(Long maNguoiDungGiangVien, Long maLopHoc) {
        LopHoc lop = timHoacNem(maLopHoc);
        kiemTraQuyenGiangVien(lop, maNguoiDungGiangVien);
        return xayDungDanhSach(lop, null, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiMucGiaoTrinhLai> layGiaoTrinhLaiChoAdmin(Long maLopHoc) {
        // Không kiểm tra quyền giảng viên — bất kỳ QTV nội dung nào cũng
        // xem được (đã chặn ở @PreAuthorize cấp Controller). Dùng chung
        // xayDungDanhSach(choGiangVien=true) để thấy đủ cả mục đang ẩn,
        // giống hệt góc nhìn giảng viên — admin cần thấy toàn bộ để giám
        // sát, không chỉ phần học viên đang thấy.
        LopHoc lop = timHoacNem(maLopHoc);
        return xayDungDanhSach(lop, null, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiMucGiaoTrinhLai> layGiaoTrinhLaiChoHocVien(Long maNguoiDung, Long maLopHoc) {
        LopHoc lop = timHoacNem(maLopHoc);
        if (!thanhVienLopKho.existsByMaHocVienAndMaLopHoc(maNguoiDung, maLopHoc)) {
            throw new NgoaiLeKhongCoQuyen("Bạn chưa tham gia lớp học này");
        }
        if (lop.getTrangThai() == TrangThaiLop.SAP_DIEN_RA) {
            // Lớp chưa khai giảng: học viên đã ghi danh/trả phí nhưng CHƯA được
            // xem nội dung (kể cả đường dẫn tệp tin/video) — trả rỗng thay vì
            // ném lỗi, để giao diện hiển thị màn hình "chờ khai giảng" bình
            // thường thay vì một trang lỗi đỏ. FE lấy ngày khai giảng/đếm
            // ngược từ API riêng layLopHocVien (đã có sẵn trangThai + ngayBatDau).
            return List.of();
        }
        return xayDungDanhSach(lop, maNguoiDung, false);
    }

    @Override
    public void capNhatCauHinhBaiGoc(Long maNguoiDungGiangVien, Long maLopHoc, Long maBaiHoc, YeuCauCauHinhBaiGoc yeuCau) {
        LopHoc lop = timHoacNem(maLopHoc);
        kiemTraQuyenGiangVien(lop, maNguoiDungGiangVien);

        CauHinhBaiGoc cauHinh = cauHinhBaiGocKho.findByLopHoc_MaLopHocAndMaBaiHoc(maLopHoc, maBaiHoc)
                .orElseGet(() -> cauHinhBaiGocKho.save(new CauHinhBaiGoc(lop, maBaiHoc)));

        if (yeuCau.anBaiHoc() != null) {
            cauHinh.setAnBaiHoc(yeuCau.anBaiHoc());
        }
        cauHinh.setViTriMoi(yeuCau.viTriMoi());
        cauHinh.setNgayCapNhat(LocalDateTime.now());
    }

    /**
     * Lõi thuật toán ghép giáo trình lai: gộp bài học gốc (đã áp dụng cấu
     * hình ẩn/vị trí mới riêng cho lớp) với bài học bổ sung, rồi sắp xếp
     * chung theo tọa độ FLOAT viTri tăng dần — đúng nguyên lý "fractional
     * indexing" đã mô tả trong Chương 2: chèn bài mới vào giữa 2 mục chỉ
     * cần tính trung bình cộng vị trí 2 mục liền kề, không phải dịch
     * chuyển lại toàn bộ các bài phía sau.
     */
    private List<PhanHoiMucGiaoTrinhLai> xayDungDanhSach(LopHoc lop, Long maNguoiDungXemHienTai, boolean choGiangVien) {
        List<PhanHoiMucGiaoTrinhLai> ketQua = new ArrayList<>();

        if (lop.getMaKhoaHoc() != null) {
            Map<Long, CauHinhBaiGoc> cauHinhTheoBaiHoc = cauHinhBaiGocKho.findByLopHoc_MaLopHoc(lop.getMaLopHoc()).stream()
                    .collect(Collectors.toMap(CauHinhBaiGoc::getMaBaiHoc, c -> c, (a, b) -> a));

            // Dùng bản đã lọc bài học "ẩn" ở cấp khóa học (BaiHoc.isAn()) —
            // một bài đã bị ẩn ở gốc thì không nên xuất hiện lại trong giáo
            // trình lai của bất kỳ lớp nào, kể cả cho Giảng viên cấu hình,
            // vì nó tương đương "đã gỡ" khỏi kho nội dung gốc.
            List<PhanHoiChuong> giaoTrinhGoc = quanLyNoiDungDichVu.layGiaoTrinhHienThi(lop.getMaKhoaHoc());
            for (PhanHoiChuong chuong : giaoTrinhGoc) {
                for (PhanHoiBaiHoc baiHoc : chuong.danhSachBaiHoc()) {
                    CauHinhBaiGoc cauHinh = cauHinhTheoBaiHoc.get(baiHoc.maBaiHoc());
                    boolean bAn = cauHinh != null && Boolean.TRUE.equals(cauHinh.getAnBaiHoc());
                    if (!choGiangVien && bAn) {
                        continue; // học viên không được thấy mục giảng viên đã ẩn
                    }

                    float viTriHieuQua = (cauHinh != null && cauHinh.getViTriMoi() != null)
                            ? cauHinh.getViTriMoi()
                            : baiHoc.viTri().floatValue();

                    // FIX (V14): phải khớp thêm maLopHoc — nếu không, hiển thị nhầm
                    // tiến độ tự học hoặc tiến độ ở lớp khác cùng khóa học.
                    java.util.Optional<TienDoBaiHoc> tienDoOpt = maNguoiDungXemHienTai == null
                            ? java.util.Optional.empty()
                            : tienDoBaiHocKho.findByMaHocVienAndMaBaiHocAndMaLopHoc(
                                    maNguoiDungXemHienTai, baiHoc.maBaiHoc(), lop.getMaLopHoc());
                    Boolean hoanThanh = maNguoiDungXemHienTai == null ? null
                            : tienDoOpt.map(TienDoBaiHoc::getHoanThanh).orElse(false);
                    Long viTriDung = tienDoOpt.map(TienDoBaiHoc::getViTriDung).orElse(null);

                    ketQua.add(new PhanHoiMucGiaoTrinhLai(
                            "GOC-" + baiHoc.maBaiHoc(), "GOC", baiHoc.maBaiHoc(), chuong.tieuDe(),
                            baiHoc.tieuDe(), null, baiHoc.loai().name(), viTriHieuQua, !bAn, baiHoc.xemThu(),
                            baiHoc.duongDan(), baiHoc.metadata(), baiHoc.baiKiemTra() != null, hoanThanh, viTriDung));
                }
            }
        }

        for (BaiHocBoSung b : baiHocBoSungKho.findByLopHoc_MaLopHocOrderByViTriAsc(lop.getMaLopHoc())) {
            boolean hien = Boolean.TRUE.equals(b.getAnHien());
            if (!choGiangVien && !hien) {
                continue;
            }
            java.util.Optional<TienDoBaiBoSung> tienDoBsOpt = maNguoiDungXemHienTai == null
                    ? java.util.Optional.empty()
                    : tienDoBaiBoSungKho.findByMaHocVienAndBaiHocBoSung_MaBaiHocBoSung(maNguoiDungXemHienTai, b.getMaBaiHocBoSung());
            Boolean hoanThanh = maNguoiDungXemHienTai == null ? null
                    : tienDoBsOpt.map(TienDoBaiBoSung::getHoanThanh).orElse(false);
            Long viTriDung = tienDoBsOpt.map(TienDoBaiBoSung::getViTriDung).orElse(null);

            ketQua.add(new PhanHoiMucGiaoTrinhLai(
                    "BS-" + b.getMaBaiHocBoSung(), "BO_SUNG", b.getMaBaiHocBoSung(), null,
                    b.getTieuDe(), b.getMoTa(), b.getLoai().name(), b.getViTri(), hien, false,
                    b.getDuongDan(), b.getDuLieuDong(), b.getLoai().name().equals("QUIZ"), hoanThanh, viTriDung));
        }

        ketQua.sort(Comparator.comparing(PhanHoiMucGiaoTrinhLai::viTri));
        return ketQua;
    }

    private LopHoc timHoacNem(Long maLopHoc) {
        return lopHocKho.findById(maLopHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học", maLopHoc));
    }

    private void kiemTraQuyenGiangVien(LopHoc lop, Long maNguoiDung) {
        if (!lop.laGiangVien(maNguoiDung)) {
            throw new NgoaiLeKhongCoQuyen("Bạn không phải giảng viên phụ trách lớp học này");
        }
    }
}