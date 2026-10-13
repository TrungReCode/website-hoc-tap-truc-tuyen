package com.elearning.lopHocLMS.ungDung.impl;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiBaiHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiChuong;
import com.elearning.choKhoaHoc.haTang.BaiHocKho;
import com.elearning.choKhoaHoc.mienDomain.BaiHoc;
import com.elearning.choKhoaHoc.ungDung.QuanLyNoiDungDichVu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCapNhatTienDo;
import com.elearning.lopHocLMS.haTang.BaiHocBoSungKho;
import com.elearning.lopHocLMS.haTang.CauHinhBaiGocKho;
import com.elearning.lopHocLMS.haTang.LopHocKho;
import com.elearning.lopHocLMS.haTang.QuyenSuDungKho;
import com.elearning.lopHocLMS.haTang.ThanhVienLopKho;
import com.elearning.lopHocLMS.haTang.TienDoBaiBoSungKho;
import com.elearning.lopHocLMS.haTang.TienDoBaiHocKho;
import com.elearning.lopHocLMS.mienDomain.BaiHocBoSung;
import com.elearning.lopHocLMS.mienDomain.CauHinhBaiGoc;
import com.elearning.lopHocLMS.mienDomain.LopHoc;
import com.elearning.lopHocLMS.mienDomain.TienDoBaiBoSung;
import com.elearning.lopHocLMS.mienDomain.TienDoBaiHoc;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;
import com.elearning.lopHocLMS.ungDung.TienDoHocDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
class TienDoHocDichVuImpl implements TienDoHocDichVu {

    private final TienDoBaiHocKho tienDoBaiHocKho;
    private final TienDoBaiBoSungKho tienDoBaiBoSungKho;
    private final ThanhVienLopKho thanhVienLopKho;
    private final LopHocKho lopHocKho;
    private final CauHinhBaiGocKho cauHinhBaiGocKho;
    private final BaiHocBoSungKho baiHocBoSungKho;
    private final QuanLyNoiDungDichVu quanLyNoiDungDichVu; // choKhoaHoc — module khác, không gây circular
    private final QuyenSuDungKho quyenSuDungKho;
    private final BaiHocKho baiHocKho;

    @Override
    public void capNhatTienDoBaiGoc(Long maNguoiDung, Long maLopHoc, Long maBaiHoc, YeuCauCapNhatTienDo yeuCau) {
        kiemTraLaThanhVien(maNguoiDung, maLopHoc);
        LopHoc lop = lopHocKho.findById(maLopHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học", maLopHoc));
        kiemTraLopDaKhaiGiang(lop.getTrangThai());

        // FIX (V14): phải khớp thêm maLopHoc — nếu không, tiến độ ghi ở đây
        // sẽ dùng CHUNG với tiến độ tự học/lớp khác của cùng khóa học (xem
        // chú thích chi tiết ở migration V14 và entity TienDoBaiHoc).
        TienDoBaiHoc tienDo = tienDoBaiHocKho.findByMaHocVienAndMaBaiHocAndMaLopHoc(maNguoiDung, maBaiHoc, maLopHoc)
                .orElseGet(() -> tienDoBaiHocKho.save(new TienDoBaiHoc(maNguoiDung, maBaiHoc, maLopHoc)));

        if (yeuCau.viTriDung() != null) {
            tienDo.setViTriDung(yeuCau.viTriDung());
        }
        if (Boolean.TRUE.equals(yeuCau.hoanThanh()) && !Boolean.TRUE.equals(tienDo.getHoanThanh())) {
            tienDo.danhDauHoanThanh();
        }
    }

    @Override
    public void capNhatTienDoBaiBoSung(Long maNguoiDung, Long maBaiHocBoSung, YeuCauCapNhatTienDo yeuCau) {
        BaiHocBoSung baiBoSung = baiHocBoSungKho.findById(maBaiHocBoSung)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học bổ sung", maBaiHocBoSung));
        kiemTraLaThanhVien(maNguoiDung, baiBoSung.getLopHoc().getMaLopHoc());
        kiemTraLopDaKhaiGiang(baiBoSung.getLopHoc().getTrangThai());

        TienDoBaiBoSung tienDo = tienDoBaiBoSungKho.findByMaHocVienAndBaiHocBoSung_MaBaiHocBoSung(maNguoiDung, maBaiHocBoSung)
                .orElseGet(() -> tienDoBaiBoSungKho.save(new TienDoBaiBoSung(maNguoiDung, baiBoSung)));

        if (yeuCau.viTriDung() != null) {
            tienDo.setViTriDung(yeuCau.viTriDung());
        }
        if (Boolean.TRUE.equals(yeuCau.hoanThanh()) && !Boolean.TRUE.equals(tienDo.getHoanThanh())) {
            tienDo.danhDauHoanThanh();
        }
    }

    @Override
    public void capNhatTienDoCaNhan(Long maNguoiDung, Long maBaiHoc, YeuCauCapNhatTienDo yeuCau) {
        BaiHoc baiHoc = baiHocKho.findById(maBaiHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học", maBaiHoc));
        Long maKhoaHoc = baiHoc.getChuong().getKhoaHoc().getMaKhoaHoc();

        // Khác capNhatTienDoBaiGoc (yêu cầu là thành viên 1 lớp): tự học chỉ
        // cần đã sở hữu khóa học (CA_NHAN hoặc DAY_HOC đều được), không quan
        // tâm có tham gia lớp nào hay không.
        if (!quyenSuDungKho.existsByMaNguoiDungAndMaKhoaHoc(maNguoiDung, maKhoaHoc)) {
            throw new NgoaiLeKhongCoQuyen("Bạn chưa sở hữu khóa học này");
        }

        // FIX (V14): tự học ghi với maLopHoc = NULL (dùng constructor 2 tham
        // số) — tách bạch với tiến độ trong bất kỳ lớp học nào, kể cả lớp
        // dùng chung khóa học này làm giáo trình gốc.
        TienDoBaiHoc tienDo = tienDoBaiHocKho.findByMaHocVienAndMaBaiHocAndMaLopHocIsNull(maNguoiDung, maBaiHoc)
                .orElseGet(() -> tienDoBaiHocKho.save(new TienDoBaiHoc(maNguoiDung, maBaiHoc)));

        if (yeuCau.viTriDung() != null) {
            tienDo.setViTriDung(yeuCau.viTriDung());
        }
        if (Boolean.TRUE.equals(yeuCau.hoanThanh()) && !Boolean.TRUE.equals(tienDo.getHoanThanh())) {
            tienDo.danhDauHoanThanh();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int tinhTienDoPhanTram(Long maNguoiDung, Long maLopHoc) {
        LopHoc lop = lopHocKho.findById(maLopHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học", maLopHoc));

        // FIX (V14): TienDoBaiHoc giờ đã tách theo maLopHoc — chỉ đếm đúng
        // tiến độ của LỚP NÀY, không còn bị "dính" tiến độ tự học hay tiến
        // độ ở lớp khác cùng khóa học nữa.
        if (lop.getTrangThai() == TrangThaiLop.SAP_DIEN_RA) {
            return 0;
        }

        List<Long> dsMaBaiHocGocHienThi = layDsMaBaiHocGocHienThi(lop);
        List<BaiHocBoSung> dsBaiBoSungHienThi = baiHocBoSungKho.findByLopHoc_MaLopHocOrderByViTriAsc(maLopHoc).stream()
                .filter(b -> Boolean.TRUE.equals(b.getAnHien()))
                .toList();

        int tongSo = dsMaBaiHocGocHienThi.size() + dsBaiBoSungHienThi.size();
        if (tongSo == 0) {
            return 0;
        }

        long soDaHoanThanhGoc = dsMaBaiHocGocHienThi.isEmpty() ? 0
                : tienDoBaiHocKho.countByMaHocVienAndMaBaiHocInAndMaLopHocAndHoanThanhTrue(
                        maNguoiDung, dsMaBaiHocGocHienThi, maLopHoc);

        long soDaHoanThanhBoSung = dsBaiBoSungHienThi.stream()
                .filter(b -> tienDoBaiBoSungKho.findByMaHocVienAndBaiHocBoSung_MaBaiHocBoSung(maNguoiDung, b.getMaBaiHocBoSung())
                        .map(TienDoBaiBoSung::getHoanThanh).orElse(false))
                .count();

        return (int) Math.round((soDaHoanThanhGoc + soDaHoanThanhBoSung) * 100.0 / tongSo);
    }

    private List<Long> layDsMaBaiHocGocHienThi(LopHoc lop) {
        if (lop.getMaKhoaHoc() == null) {
            return List.of();
        }
        Set<Long> dsMaAn = cauHinhBaiGocKho.findByLopHoc_MaLopHoc(lop.getMaLopHoc()).stream()
                .filter(CauHinhBaiGoc::getAnBaiHoc)
                .map(CauHinhBaiGoc::getMaBaiHoc)
                .collect(Collectors.toSet());

        List<PhanHoiChuong> giaoTrinh = quanLyNoiDungDichVu.layGiaoTrinhHienThi(lop.getMaKhoaHoc());
        return giaoTrinh.stream()
                .flatMap(c -> c.danhSachBaiHoc().stream())
                .map(PhanHoiBaiHoc::maBaiHoc)
                .filter(maBaiHoc -> !dsMaAn.contains(maBaiHoc))
                .toList();
    }

    private void kiemTraLaThanhVien(Long maNguoiDung, Long maLopHoc) {
        // FIX: quay lại chặn chặt — CHỈ học viên thật mới được ghi tiến độ
        // (capNhatTienDoBaiGoc/capNhatTienDoBaiBoSung), không cho phép Giảng
        // viên. Trước đây có mở thêm nhánh cho phép Giảng viên đi qua, lý do
        // là lúc đó nopBaiGoc() (KiemTraTrongLopDichVuImpl) dùng CHUNG guard
        // "kiemTraLaThanhVien" cho cả xem đề lẫn nộp bài — nếu chặn ở đây thì
        // Giảng viên tự làm thử Quiz đạt điểm cũng bị lỗi "chưa tham gia lớp".
        //
        // Giờ đã tách hẳn: KiemTraTrongLopDichVuImpl có kiemTraDuocNopBai()
        // riêng cho hành động nộp bài (chặn Giảng viên ngay từ đó), nên
        // capNhatTienDoBaiGoc/BoSung ở đây không còn được gọi từ luồng nộp
        // bài của Giảng viên nữa — có thể xiết chặt lại như thiết kế gốc,
        // tránh Giảng viên tạo tiến độ "ảo" cho chính lớp mình dạy (từng dẫn
        // tới việc đủ điều kiện tự cấp Giấy xác nhận lớp học cho bản thân).
        if (!thanhVienLopKho.existsByMaHocVienAndMaLopHoc(maNguoiDung, maLopHoc)) {
            throw new NgoaiLeKhongCoQuyen("Bạn chưa tham gia lớp học này");
        }
    }

    /**
     * Chặn học viên tự gọi thẳng API cập nhật tiến độ trước ngày khai giảng
     * (phòng trường hợp bỏ qua giao diện — giao diện đã ẩn nội dung bài học
     * khi lớp còn SAP_DIEN_RA thông qua GiaoTrinhLaiDichVu.layGiaoTrinhLaiChoHocVien).
     */
    private void kiemTraLopDaKhaiGiang(TrangThaiLop trangThai) {
        if (trangThai == TrangThaiLop.SAP_DIEN_RA) {
            throw new NgoaiLeDuLieuKhongHopLe("Lớp học chưa khai giảng, chưa thể cập nhật tiến độ học");
        }
    }
}