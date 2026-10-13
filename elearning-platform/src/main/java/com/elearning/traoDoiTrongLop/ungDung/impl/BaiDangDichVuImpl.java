package com.elearning.traoDoiTrongLop.ungDung.impl;

import com.elearning.dinhDanhPhanQuyen.ungDung.HoSoCongKhaiDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.ThongTinNguoiDungCoBan;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.dungChung.phanTrang.KetQuaPhanTrang;
import com.elearning.lopHocLMS.ungDung.GhiDanhDichVu;
import com.elearning.lopHocLMS.ungDung.TaoLopHocDichVu;
import com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi.PhanHoiBaiDang;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauCapNhatBaiDang;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauTaoBaiDang;
import com.elearning.traoDoiTrongLop.haTang.BaiDangKho;
import com.elearning.traoDoiTrongLop.haTang.BinhLuanKho;
import com.elearning.traoDoiTrongLop.haTang.ThongBaoKho;
import com.elearning.traoDoiTrongLop.haTang.boPhanNgoai.AdapterLuuTruDinhKem;
import com.elearning.traoDoiTrongLop.haTang.boPhanNgoai.KetQuaTaiLenDinhKem;
import com.elearning.traoDoiTrongLop.mienDomain.BaiDang;
import com.elearning.traoDoiTrongLop.mienDomain.BinhLuan;
import com.elearning.traoDoiTrongLop.mienDomain.TepDinhKem;
import com.elearning.traoDoiTrongLop.mienDomain.ThongBao;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiBaiDang;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;
import com.elearning.traoDoiTrongLop.ungDung.BaiDangDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
class BaiDangDichVuImpl implements BaiDangDichVu {

    /** Giới hạn dung lượng file đính kèm bài đăng — nhỏ hơn nhiều so với video bài giảng (500MB). */
    private static final long GIOI_HAN_DUNG_LUONG_DINH_KEM = 20L * 1024 * 1024; // 20MB

    private final BaiDangKho       baiDangKho;
    private final BinhLuanKho      binhLuanKho;
    private final ThongBaoKho      thongBaoKho;
    private final GhiDanhDichVu    ghiDanhDichVu;
    private final AdapterLuuTruDinhKem adapterLuuTruDinhKem;
    private final TaoLopHocDichVu  taoLopHocDichVu;
    private final HoSoCongKhaiDichVu hoSoCongKhaiDichVu;

    // ──────────────── CRUD bài đăng ────────────────

    @Override
    public PhanHoiBaiDang taoBaiDang(Long maNguoiDung, Long maLopHoc, YeuCauTaoBaiDang yeuCau) {
        kiemTraTruyCap(maNguoiDung, maLopHoc);

        // Chỉ giảng viên được đăng loại THONG_BAO hoặc BAI_TAP
        if (yeuCau.loai() == LoaiBaiDang.THONG_BAO || yeuCau.loai() == LoaiBaiDang.BAI_TAP) {
            kiemTraLaGiangVien(maNguoiDung, maLopHoc);
        }

        BaiDang baiDang = new BaiDang(
                maLopHoc, maNguoiDung, yeuCau.loai(),
                yeuCau.tieuDe(), yeuCau.noiDung(), yeuCau.tepDinhKem());
        baiDangKho.save(baiDang);

        // Gửi thông báo cho các thành viên khác trong lớp (ngoại trừ người đăng)
        guiThongBaoBaiDangMoi(baiDang, maNguoiDung, maLopHoc);

        return sangPhanHoi(baiDang);
    }

    @Override
    public PhanHoiBaiDang capNhatBaiDang(Long maNguoiDung, Long maBaiDang, YeuCauCapNhatBaiDang yeuCau) {
        BaiDang baiDang = timHoacNem(maBaiDang);
        // Chỉ chủ sở hữu hoặc giảng viên lớp được sửa
        if (!baiDang.laChuSoHuu(maNguoiDung)) {
            kiemTraLaGiangVien(maNguoiDung, baiDang.getMaLopHoc());
        }
        baiDang.capNhat(yeuCau.tieuDe(), yeuCau.noiDung(), yeuCau.tepDinhKem());
        return sangPhanHoi(baiDang);
    }

    @Override
    public void xoaBaiDang(Long maNguoiDung, Long maBaiDang) {
        BaiDang baiDang = timHoacNem(maBaiDang);
        boolean laGiangVien = taoLopHocDichVu.laGiangVienCuaLop(maNguoiDung, baiDang.getMaLopHoc());
        if (!baiDang.laChuSoHuu(maNguoiDung) && !laGiangVien) {
            throw new NgoaiLeKhongCoQuyen("Bạn không có quyền xóa bài đăng này");
        }

        baiDang.xoaMem(); // soft-delete bài viết, giữ nguyên bản ghi để tránh mất dữ liệu

        // Đặc quyền kiểm duyệt của Giảng viên: xóa bài kèm xóa lan truyền toàn
        // bộ cây bình luận phụ thuộc phía dưới. Học viên tự xóa bài của mình
        // thì KHÔNG cascade — giữ nguyên bình luận trong DB (đúng luồng chính
        // "Tham gia thảo luận").
        if (laGiangVien) {
            List<BinhLuan> binhLuans = binhLuanKho.findByMaBaiDang(maBaiDang);
            if (!binhLuans.isEmpty()) {
                binhLuanKho.deleteAll(binhLuans);
            }
        }
    }

    @Override
    public PhanHoiBaiDang danhDauGiaiDap(Long maNguoiDungGiangVien, Long maBaiDang, boolean giaiDap) {
        BaiDang baiDang = timHoacNem(maBaiDang);
        kiemTraLaGiangVien(maNguoiDungGiangVien, baiDang.getMaLopHoc());

        if (baiDang.getLoai() != LoaiBaiDang.HOI_DAP) {
            throw new NgoaiLeDuLieuKhongHopLe("Chỉ bài đăng loại HỎI ĐÁP mới có thể đánh dấu giải đáp");
        }
        baiDang.danhDauGiaiDap(giaiDap);

        // Gửi thông báo cho người đặt câu hỏi (nếu đánh dấu = đã giải đáp)
        if (giaiDap && !baiDang.getMaNguoiDang().equals(maNguoiDungGiangVien)) {
            ThongTinNguoiDungCoBan gv = hoSoCongKhaiDichVu.layThongTinCoBan(maNguoiDungGiangVien).orElse(null);
            String tenGV = gv != null ? gv.hoTen() : "Giảng viên";

            Map<String, Object> du = new HashMap<>();
            du.put("maBaiDang", baiDang.getMaBaiDang());
            du.put("maLopHoc",  baiDang.getMaLopHoc());
            du.put("tieuDeBaiDang", baiDang.getTieuDe());

            thongBaoKho.save(new ThongBao(
                    baiDang.getMaNguoiDang(),
                    LoaiThongBao.GIAI_DAP,
                    "Câu hỏi của bạn đã được giải đáp",
                    tenGV + " đã đánh dấu giải đáp bài \"" + baiDang.getTieuDe() + "\"",
                    du));
        }
        return sangPhanHoi(baiDang);
    }

    @Override
    @Transactional(readOnly = true)
    public PhanHoiBaiDang layChiTiet(Long maNguoiDung, Long maBaiDang) {
        BaiDang baiDang = timHoacNem(maBaiDang);
        kiemTraTruyCap(maNguoiDung, baiDang.getMaLopHoc());
        return sangPhanHoi(baiDang);
    }

    @Override
    @Transactional(readOnly = true)
    public KetQuaPhanTrang<PhanHoiBaiDang> layDanhSach(Long maNguoiDung, Long maLopHoc,
                                                        LoaiBaiDang loai, Pageable pageable) {
        kiemTraTruyCap(maNguoiDung, maLopHoc);

        Page<BaiDang> page = (loai != null)
                ? baiDangKho.findByMaLopHocAndLoaiAndXoaMemFalseOrderByNgayTaoDesc(maLopHoc, loai, pageable)
                : baiDangKho.findByMaLopHocAndXoaMemFalseOrderByNgayTaoDesc(maLopHoc, pageable);

        // Batch-load thông tin người dùng để tránh N+1
        List<Long> dsMa = page.getContent().stream().map(BaiDang::getMaNguoiDang).distinct().toList();
        Map<Long, ThongTinNguoiDungCoBan> bangNguoiDung = hoSoCongKhaiDichVu.layThongTinCoBanNhieu(dsMa);

        // FIX: batch-load luôn số bình luận theo cùng cách trên — trước đây
        // đoạn này thiếu, khiến sangPhanHoiVoiBang() gọi countByMaBaiDang()
        // riêng cho từng bài đăng trong trang kết quả (N+1 dù đã tránh được
        // N+1 cho phần thông tin người dùng ở trên). Bài đăng không có mặt
        // trong bảng (chưa có bình luận nào) mặc định lấy 0 khi tra ở dưới.
        List<Long> dsMaBaiDang = page.getContent().stream().map(BaiDang::getMaBaiDang).toList();
        Map<Long, Long> bangSoBinhLuan = dsMaBaiDang.isEmpty()
                ? Map.of()
                : binhLuanKho.demTheoDanhSachBaiDang(dsMaBaiDang).stream()
                        .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

        Page<PhanHoiBaiDang> ketQua = page.map(b -> sangPhanHoiVoiBang(b, bangNguoiDung, bangSoBinhLuan));
        return KetQuaPhanTrang.tuPage(ketQua);
    }

    @Override
    public TepDinhKem taiLenTepDinhKem(Long maNguoiDung, Long maLopHoc, MultipartFile file) {
        kiemTraTruyCap(maNguoiDung, maLopHoc);

        if (file == null || file.isEmpty()) {
            throw new NgoaiLeDuLieuKhongHopLe("Vui lòng chọn tệp để đính kèm");
        }
        if (file.getSize() > GIOI_HAN_DUNG_LUONG_DINH_KEM) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Tệp đính kèm tối đa 20MB (tệp của bạn: " + (file.getSize() / (1024 * 1024)) + "MB)");
        }

        KetQuaTaiLenDinhKem ketQua = adapterLuuTruDinhKem.taiLen(file);
        return new TepDinhKem(file.getOriginalFilename(), ketQua.url(), phanLoaiTep(ketQua.loaiTep(), file.getContentType()));
    }

    /** Phân loại đơn giản theo đuôi tệp/MIME để khớp TepDinhKem.loai ("PDF"|"IMAGE"|"VIDEO"|"OTHER"). */
    private String phanLoaiTep(String duoiTep, String contentType) {
        if (contentType != null) {
            if (contentType.startsWith("image/")) return "IMAGE";
            if (contentType.startsWith("video/")) return "VIDEO";
            if (contentType.equals("application/pdf")) return "PDF";
        }
        if (duoiTep != null) {
            String d = duoiTep.toLowerCase();
            if (d.equals("pdf")) return "PDF";
            if (List.of("jpg", "jpeg", "png", "gif", "webp").contains(d)) return "IMAGE";
            if (List.of("mp4", "mov", "avi", "mkv", "webm").contains(d)) return "VIDEO";
        }
        return "OTHER";
    }

    // ──────────────── Helper ────────────────

    private BaiDang timHoacNem(Long maBaiDang) {
        BaiDang b = baiDangKho.findById(maBaiDang)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài đăng", maBaiDang));
        if (Boolean.TRUE.equals(b.getXoaMem())) {
            throw NgoaiLeKhongTimThay.cuaTaiNguyen("bài đăng", maBaiDang);
        }
        return b;
    }

    /** Kiểm tra người dùng là thành viên HOẶC giảng viên của lớp. */
    private void kiemTraTruyCap(Long maNguoiDung, Long maLopHoc) {
        boolean laGiangVien = taoLopHocDichVu.laGiangVienCuaLop(maNguoiDung, maLopHoc);
        boolean laThanhVien = ghiDanhDichVu.laThanhVien(maNguoiDung, maLopHoc);
        if (!laGiangVien && !laThanhVien) {
            throw new NgoaiLeKhongCoQuyen("Bạn không phải thành viên của lớp học này");
        }
    }

    private void kiemTraLaGiangVien(Long maNguoiDung, Long maLopHoc) {
        if (!taoLopHocDichVu.laGiangVienCuaLop(maNguoiDung, maLopHoc)) {
            throw new NgoaiLeKhongCoQuyen("Chỉ giảng viên của lớp mới có quyền thực hiện thao tác này");
        }
    }

    private void guiThongBaoBaiDangMoi(BaiDang baiDang, Long maNguoiDang, Long maLopHoc) {
        ThongTinNguoiDungCoBan nguoiDang = hoSoCongKhaiDichVu.layThongTinCoBan(maNguoiDang).orElse(null);
        String tenNguoiDang = nguoiDang != null ? nguoiDang.hoTen() : "Một thành viên";

        Map<String, Object> du = new HashMap<>();
        du.put("maBaiDang",     baiDang.getMaBaiDang());
        du.put("maLopHoc",      maLopHoc);
        du.put("tieuDeBaiDang", baiDang.getTieuDe());

        // Gửi cho tất cả thành viên trong lớp trừ người đăng
        ghiDanhDichVu.layThanhVienCuaLop(maLopHoc).stream()
                .filter(tv -> !tv.maHocVien().equals(maNguoiDang))
                .forEach(tv -> thongBaoKho.save(new ThongBao(
                        tv.maHocVien(),
                        LoaiThongBao.BAI_DANG_MOI,
                        "Bài đăng mới trong lớp học",
                        tenNguoiDang + " đã đăng: \"" + baiDang.getTieuDe() + "\"",
                        new HashMap<>(du))));
    }

    private PhanHoiBaiDang sangPhanHoi(BaiDang b) {
        ThongTinNguoiDungCoBan nd = hoSoCongKhaiDichVu.layThongTinCoBan(b.getMaNguoiDang()).orElse(null);
        long soBinhLuan = binhLuanKho.countByMaBaiDang(b.getMaBaiDang());
        return new PhanHoiBaiDang(
                b.getMaBaiDang(), b.getMaLopHoc(), b.getMaNguoiDang(),
                nd != null ? nd.hoTen() : null,
                nd != null ? nd.anhDaiDien() : null,
                b.getLoai(), b.getTieuDe(), b.getNoiDung(), b.getTepDinhKem(),
                b.getGiaiDap(), soBinhLuan, b.getNgayTao(), b.getNgayCapNhat());
    }

    private PhanHoiBaiDang sangPhanHoiVoiBang(BaiDang b, Map<Long, ThongTinNguoiDungCoBan> bang,
            Map<Long, Long> bangSoBinhLuan) {
        ThongTinNguoiDungCoBan nd = bang.get(b.getMaNguoiDang());
        long soBinhLuan = bangSoBinhLuan.getOrDefault(b.getMaBaiDang(), 0L);
        return new PhanHoiBaiDang(
                b.getMaBaiDang(), b.getMaLopHoc(), b.getMaNguoiDang(),
                nd != null ? nd.hoTen() : null,
                nd != null ? nd.anhDaiDien() : null,
                b.getLoai(), b.getTieuDe(), b.getNoiDung(), b.getTepDinhKem(),
                b.getGiaiDap(), soBinhLuan, b.getNgayTao(), b.getNgayCapNhat());
    }
}