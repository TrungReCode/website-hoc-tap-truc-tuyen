package com.elearning.lopHocLMS.ungDung.impl;

import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiCauHoiBoSung;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiMucGiaoTrinhLai;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCapNhatBaiBoSung;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauDapAnBoSung;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauTaoBaiBoSung;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauTaoCauHoiBoSung;
import com.elearning.lopHocLMS.haTang.BaiHocBoSungKho;
import com.elearning.lopHocLMS.haTang.BaiKiemTraBoSungKho;
import com.elearning.lopHocLMS.haTang.CauHoiBoSungKho;
import com.elearning.lopHocLMS.haTang.LopHocKho;
import com.elearning.lopHocLMS.haTang.TienDoBaiBoSungKho;
import com.elearning.lopHocLMS.haTang.boPhanNgoai.AdapterLuuTruBoSung;
import com.elearning.lopHocLMS.mienDomain.BaiHocBoSung;
import com.elearning.lopHocLMS.mienDomain.BaiKiemTraBoSung;
import com.elearning.lopHocLMS.mienDomain.CauHoiBoSung;
import com.elearning.lopHocLMS.mienDomain.DapAnBoSung;
import com.elearning.lopHocLMS.mienDomain.LopHoc;
import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiCauHoiBoSung;
import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiHocLieuBoSung;
import com.elearning.lopHocLMS.ungDung.BaiBoSungDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
class BaiBoSungDichVuImpl implements BaiBoSungDichVu {

    private final LopHocKho lopHocKho;
    private final BaiHocBoSungKho baiHocBoSungKho;
    private final BaiKiemTraBoSungKho baiKiemTraBoSungKho;
    private final CauHoiBoSungKho cauHoiBoSungKho;
    private final AdapterLuuTruBoSung adapterLuuTruBoSung;
    private final TienDoBaiBoSungKho tienDoBaiBoSungKho;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    // FIX: cùng lý do như QuanLyNoiDungDichVuImpl.themBaiHoc — adapterLuuTruBoSung.taiLen()
    // (upload file lên storage ngoài) trước đây chạy trong transaction ghi
    // thật của class, giữ connection CSDL suốt lúc upload file lớn/mạng
    // chậm. Tạm ngưng transaction cho toàn hàm; các save() bên dưới vẫn an
    // toàn vì JpaRepository.save() tự có transaction riêng cho từng lệnh.
    public PhanHoiMucGiaoTrinhLai themBaiBoSung(Long maNguoiDungGiangVien, Long maLopHoc,
                                                  YeuCauTaoBaiBoSung yeuCau, MultipartFile file) {
        LopHoc lop = timLopHoacNem(maLopHoc);
        kiemTraQuyenGiangVien(lop, maNguoiDungGiangVien);

        float viTriMoi = tinhViTriMoi(maLopHoc, yeuCau.viTriSau(), yeuCau.viTriTruoc());
        BaiHocBoSung baiBoSung = new BaiHocBoSung(lop, yeuCau.tieuDe(), yeuCau.moTa(), yeuCau.loai(), viTriMoi);
        baiBoSung.setAnHien(yeuCau.anHien());

        switch (yeuCau.loai()) {
            case LINK -> baiBoSung.setDuongDan(yeuCau.duongDanLink() != null ? yeuCau.duongDanLink() : "");
            case QUIZ -> baiBoSung.setDuongDan("");
            default -> {
                if (file == null || file.isEmpty()) {
                    throw new NgoaiLeDuLieuKhongHopLe("Vui lòng chọn tệp để tải lên");
                }
                var kq = adapterLuuTruBoSung.taiLen(file);
                baiBoSung.setDuongDan(kq.url());
                baiBoSung.setLoaiTep(kq.loaiTep());
                baiBoSung.setDuLieuDong(Map.of("kichThuocByte", kq.kichThuocByte()));
            }
        }
        baiHocBoSungKho.save(baiBoSung);

        if (yeuCau.loai() == LoaiHocLieuBoSung.QUIZ) {
            BaiKiemTraBoSung bkt = new BaiKiemTraBoSung(baiBoSung, yeuCau.diemToiThieu(), yeuCau.thoiGian());
            baiKiemTraBoSungKho.save(bkt);
        }
        return sangPhanHoi(baiBoSung);
    }

    @Override
    public PhanHoiMucGiaoTrinhLai capNhatBaiBoSung(Long maNguoiDungGiangVien, Long maBaiHocBoSung,
                                                     YeuCauCapNhatBaiBoSung yeuCau) {
        BaiHocBoSung b = timBaiHoacNem(maBaiHocBoSung);
        kiemTraQuyenGiangVien(b.getLopHoc(), maNguoiDungGiangVien);
        b.setTieuDe(yeuCau.tieuDe());
        b.setMoTa(yeuCau.moTa());
        b.setAnHien(yeuCau.anHien());
        // Chỉ tính lại vị trí khi FE thực sự gửi lên (thao tác di chuyển lên/xuống).
        // Không đụng vào viTri khi chỉ sửa tiêu đề/mô tả/ẩn-hiện thông thường.
        if (yeuCau.viTriSau() != null || yeuCau.viTriTruoc() != null) {
            b.setViTri(tinhViTriMoi(b.getLopHoc().getMaLopHoc(), yeuCau.viTriSau(), yeuCau.viTriTruoc()));
        }
        b.danhDauCapNhat();
        return sangPhanHoi(b);
    }

    @Override
    public void xoaBaiBoSung(Long maNguoiDungGiangVien, Long maBaiHocBoSung) {
        BaiHocBoSung b = timBaiHoacNem(maBaiHocBoSung);
        kiemTraQuyenGiangVien(b.getLopHoc(), maNguoiDungGiangVien);

        // Ràng buộc xóa học liệu bổ sung: nếu đã có học viên phát sinh dữ
        // liệu ghi nhận tiến độ trên bài học này, chặn xóa cứng và ép
        // chuyển sang trạng thái Ẩn (anHien=false, sửa qua capNhatBaiBoSung)
        // để không làm mất tiến độ/điểm số học viên đã có.
        if (tienDoBaiBoSungKho.existsByBaiHocBoSung_MaBaiHocBoSung(maBaiHocBoSung)) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Không thể xóa vật lý bài học bổ sung vì đã có học viên ghi nhận tiến độ trên bài này. "
                            + "Vui lòng chuyển sang trạng thái ẩn (anHien=false) thay vì xóa.");
        }

        // CASCADE ở DB (V8 migration) tự xóa bai_kiem_tra_bo_sung + cau_hoi_bai_bo_sung
        baiHocBoSungKho.delete(b);

        // FIX: dọn file trên R2 sau khi xóa DB thành công — bỏ qua LINK (URL
        // ngoài, không phải file của mình) và QUIZ (không có file).
        if (b.getLoai() != LoaiHocLieuBoSung.LINK && b.getLoai() != LoaiHocLieuBoSung.QUIZ) {
            adapterLuuTruBoSung.xoa(b.getDuongDan());
        }
    }

    @Override
    public void themCauHoiBoSung(Long maNguoiDungGiangVien, Long maBaiHocBoSung, YeuCauTaoCauHoiBoSung yeuCau) {
        BaiHocBoSung b = timBaiHoacNem(maBaiHocBoSung);
        kiemTraQuyenGiangVien(b.getLopHoc(), maNguoiDungGiangVien);

        if (b.getLoai() != LoaiHocLieuBoSung.QUIZ) {
            throw new NgoaiLeDuLieuKhongHopLe("Bài học bổ sung không phải loại Quiz");
        }
        BaiKiemTraBoSung bkt = baiKiemTraBoSungKho.findByBaiBoSung_MaBaiHocBoSung(maBaiHocBoSung)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài kiểm tra bổ sung cho bài", maBaiHocBoSung));

        boolean coDapAnDung = yeuCau.dapAn().stream().anyMatch(YeuCauDapAnBoSung::dapAnDung);
        if (!coDapAnDung) {
            throw new NgoaiLeDuLieuKhongHopLe("Câu hỏi cần có ít nhất 1 đáp án đúng");
        }

        LoaiCauHoiBoSung loai = parseLoai(yeuCau.loaiCauHoi());
        List<DapAnBoSung> dapAn = yeuCau.dapAn().stream()
                .map(d -> new DapAnBoSung(d.id(), d.noiDung(), d.dapAnDung())).toList();
        cauHoiBoSungKho.save(new CauHoiBoSung(bkt, yeuCau.noiDung(), yeuCau.viTri(), loai, dapAn));
    }

    @Override
    public void xoaCauHoiBoSung(Long maNguoiDungGiangVien, Long maCauHoiBoSung) {
        CauHoiBoSung c = cauHoiBoSungKho.findById(maCauHoiBoSung)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("câu hỏi bổ sung", maCauHoiBoSung));
        kiemTraQuyenGiangVien(c.getBaiKiemTraBoSung().getBaiBoSung().getLopHoc(), maNguoiDungGiangVien);
        cauHoiBoSungKho.delete(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiCauHoiBoSung> layCauHoiBoSung(Long maNguoiDungGiangVien, Long maBaiHocBoSung) {
        BaiHocBoSung b = timBaiHoacNem(maBaiHocBoSung);
        kiemTraQuyenGiangVien(b.getLopHoc(), maNguoiDungGiangVien);
        if (b.getLoai() != LoaiHocLieuBoSung.QUIZ) {
            throw new NgoaiLeDuLieuKhongHopLe("Bài học bổ sung không phải loại Quiz");
        }
        BaiKiemTraBoSung bkt = baiKiemTraBoSungKho.findByBaiBoSung_MaBaiHocBoSung(maBaiHocBoSung)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài kiểm tra bổ sung cho bài", maBaiHocBoSung));

        return cauHoiBoSungKho.findByBaiKiemTraBoSung_MaBaiKiemTraBoSungOrderByViTriAsc(bkt.getMaBaiKiemTraBoSung())
                .stream()
                .map(c -> new PhanHoiCauHoiBoSung(c.getMaCauHoiBaiBoSung(), c.getNoiDung(), c.getViTri(),
                        c.getLoaiCauHoi().name(), c.getDapAn()))
                .toList();
    }

    // ---------- Thuật toán fractional indexing ----------

    /**
     * Tính vị trí FLOAT mới cho bài bổ sung sắp được chèn vào giữa viTriSau và viTriTruoc.
     * Cách gọi từ frontend (mockup S26 sắp xếp bằng nút lên/xuống):
     *   - Chèn vào cuối: viTriSau = vị trí mục cuối cùng, viTriTruoc = null → trả viTriSau + 1
     *   - Chèn vào đầu: viTriSau = null, viTriTruoc = vị trí mục đầu tiên → trả viTriTruoc - 1
     *   - Chèn vào giữa: cả 2 đều không null → trả trung bình cộng
     *   - Danh sách rỗng: cả 2 null → trả 1.0
     */
    private float tinhViTriMoi(Long maLopHoc, Float viTriSau, Float viTriTruoc) {
        if (viTriSau == null && viTriTruoc == null) {
            List<BaiHocBoSung> hienCo = baiHocBoSungKho.findByLopHoc_MaLopHocOrderByViTriAsc(maLopHoc);
            return hienCo.isEmpty() ? 1.0f : hienCo.get(hienCo.size() - 1).getViTri() + 1.0f;
        }
        if (viTriSau == null) return viTriTruoc - 1.0f;
        if (viTriTruoc == null) return viTriSau + 1.0f;
        return (viTriSau + viTriTruoc) / 2.0f;
    }

    // ---------- Hỗ trợ ----------

    private PhanHoiMucGiaoTrinhLai sangPhanHoi(BaiHocBoSung b) {
        boolean coBaiKiemTra = b.getLoai() == LoaiHocLieuBoSung.QUIZ
                && baiKiemTraBoSungKho.findByBaiBoSung_MaBaiHocBoSung(b.getMaBaiHocBoSung()).isPresent();
        return new PhanHoiMucGiaoTrinhLai(
                "BS-" + b.getMaBaiHocBoSung(), "BO_SUNG", b.getMaBaiHocBoSung(), null,
                b.getTieuDe(), b.getMoTa(), b.getLoai().name(), b.getViTri(),
                Boolean.TRUE.equals(b.getAnHien()), false, b.getDuongDan(), b.getDuLieuDong(), coBaiKiemTra, null, null);
    }

    private LoaiCauHoiBoSung parseLoai(String s) {
        if (s == null || s.isBlank()) return LoaiCauHoiBoSung.MOT_DAP_AN;
        try { return LoaiCauHoiBoSung.valueOf(s); }
        catch (IllegalArgumentException e) { throw new NgoaiLeDuLieuKhongHopLe("Loại câu hỏi không hợp lệ: " + s); }
    }

    private LopHoc timLopHoacNem(Long maLopHoc) {
        return lopHocKho.findById(maLopHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học", maLopHoc));
    }

    private BaiHocBoSung timBaiHoacNem(Long id) {
        return baiHocBoSungKho.findById(id)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học bổ sung", id));
    }

    private void kiemTraQuyenGiangVien(LopHoc lop, Long maNguoiDung) {
        if (!lop.laGiangVien(maNguoiDung)) {
            throw new NgoaiLeKhongCoQuyen("Bạn không phải giảng viên phụ trách lớp học này");
        }
    }
}