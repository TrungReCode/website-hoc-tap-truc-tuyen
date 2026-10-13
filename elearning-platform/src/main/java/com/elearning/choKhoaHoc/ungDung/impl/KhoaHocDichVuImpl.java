package com.elearning.choKhoaHoc.ungDung.impl;
 
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiBaiHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiChuong;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhSachKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauCapNhatKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoKhoaHoc;
import com.elearning.choKhoaHoc.haTang.DanhMucKho;
import com.elearning.choKhoaHoc.haTang.KhoaHocKho;
import com.elearning.choKhoaHoc.haTang.BaiKiemTraKho;
import com.elearning.choKhoaHoc.haTang.CauHoiKho;
import com.elearning.choKhoaHoc.mienDomain.BaiHoc;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.LoaiBaiHoc;
import com.elearning.choKhoaHoc.mienDomain.DanhMuc;
import com.elearning.choKhoaHoc.mienDomain.KhoaHoc;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.TrangThaiKhoaHoc;
import com.elearning.choKhoaHoc.ungDung.KhoaHocDichVu;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.VaiTro;
import com.elearning.dinhDanhPhanQuyen.ungDung.NguoiDungTraCuuDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.XacThucGiangVienDichVu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.lopHocLMS.haTang.QuyenSuDungKho;
import com.elearning.lopHocLMS.haTang.TienDoBaiHocKho;
import com.elearning.lopHocLMS.mienDomain.QuyenSuDung;
import com.elearning.lopHocLMS.mienDomain.TienDoBaiHoc;
import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiGiayPhep;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;
import com.elearning.traoDoiTrongLop.ungDung.ThongBaoDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
 
@Service
@RequiredArgsConstructor
@Transactional
class KhoaHocDichVuImpl implements KhoaHocDichVu {
 
    private final KhoaHocKho khoaHocKho;
    private final DanhMucKho danhMucKho;
    private final KhoaHocMapper khoaHocMapper;
    private final QuyenSuDungKho quyenSuDungKho;
    private final ThongBaoDichVu thongBaoDichVu;
    private final NguoiDungTraCuuDichVu nguoiDungTraCuuDichVu;
    private final XacThucGiangVienDichVu xacThucGiangVienDichVu;
    private final TienDoBaiHocKho tienDoBaiHocKho;
    private final BaiKiemTraKho baiKiemTraKho;
    private final CauHoiKho cauHoiKho;
 
    @Override
    public PhanHoiKhoaHoc taoKhoaHoc(Long maNguoiDung, YeuCauTaoKhoaHoc yeuCau) {
        // YÊU CẦU NGHIỆP VỤ: phải được Quản trị viên duyệt xác thực chuyên
        // môn (>= 18 tuổi + bằng cấp/chứng chỉ có minh chứng) TRƯỚC KHI được
        // phép tạo bất kỳ khóa học nào — đảm bảo chất lượng người tạo nội
        // dung, tách biệt khỏi việc kiểm duyệt NỘI DUNG khóa học
        // (DuyetKhoaHocDichVu, vốn chỉ soát nội dung sau khi đã tạo).
        if (!xacThucGiangVienDichVu.daDuocXacThuc(maNguoiDung)) {
            throw new NgoaiLeKhongCoQuyen(
                    "Bạn cần được Quản trị viên xác thực chuyên môn giảng viên trước khi tạo khóa học. "
                            + "Vui lòng gửi yêu cầu xác thực (bằng cấp/chứng chỉ) tại trang hồ sơ cá nhân.");
        }

        DanhMuc danhMuc = danhMucKho.findById(yeuCau.maDanhMuc())
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("danh mục", yeuCau.maDanhMuc()));
 
        KhoaHoc khoaHoc = new KhoaHoc(maNguoiDung, danhMuc, yeuCau.tieuDe(), yeuCau.mieuTa());
        khoaHoc.setGia(yeuCau.gia());
        khoaHoc.setPhiDayHoc(yeuCau.phiDayHoc());
        khoaHoc.setAnhBia(yeuCau.anhBia());
        khoaHocKho.save(khoaHoc);
 
        return khoaHocMapper.sangPhanHoiChiTiet(khoaHoc);
    }
 
    @Override
    public PhanHoiKhoaHoc capNhatKhoaHoc(Long maNguoiDung, Long maKhoaHoc, YeuCauCapNhatKhoaHoc yeuCau) {
        KhoaHoc khoaHoc = timHoacNem(maKhoaHoc);
        kiemTraQuyenTacGia(khoaHoc, maNguoiDung);
 
        if (khoaHoc.getTrangThai() == TrangThaiKhoaHoc.CHO_KIEM_DUYET
                || khoaHoc.getTrangThai() == TrangThaiKhoaHoc.CONG_KHAI) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Khóa học đang " + (khoaHoc.getTrangThai() == TrangThaiKhoaHoc.CONG_KHAI ? "công khai" : "chờ kiểm duyệt")
                            + ", không thể chỉnh sửa trực tiếp. "
                            + (khoaHoc.getTrangThai() == TrangThaiKhoaHoc.CONG_KHAI
                                    ? "Vui lòng lưu trữ khóa học trước khi chỉnh sửa, sau đó công khai lại."
                                    : "Vui lòng rút lại yêu cầu duyệt trước."));
        }
 
        DanhMuc danhMuc = danhMucKho.findById(yeuCau.maDanhMuc())
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("danh mục", yeuCau.maDanhMuc()));
 
        khoaHoc.setTieuDe(yeuCau.tieuDe());
        khoaHoc.setMieuTa(yeuCau.mieuTa());
        khoaHoc.setDanhMuc(danhMuc);
        khoaHoc.setGia(yeuCau.gia());
        khoaHoc.setPhiDayHoc(yeuCau.phiDayHoc());
        khoaHoc.setAnhBia(yeuCau.anhBia());
        khoaHoc.danhDauCapNhat();
 
        return khoaHocMapper.sangPhanHoiChiTiet(khoaHoc);
    }
 
    @Override
    public void xoaKhoaHoc(Long maNguoiDung, Long maKhoaHoc) {
        KhoaHoc khoaHoc = timHoacNem(maKhoaHoc);
        kiemTraQuyenTacGia(khoaHoc, maNguoiDung);
 
        if (khoaHoc.getTrangThai() != TrangThaiKhoaHoc.NHAP) {
            throw new NgoaiLeDuLieuKhongHopLe("Chỉ có thể xóa khóa học đang ở trạng thái Nháp");
        }
 
        khoaHocKho.delete(khoaHoc);
    }
 
    @Override
    @Transactional(readOnly = true)
    public PhanHoiKhoaHoc layChiTietChoTacGia(Long maNguoiDung, Long maKhoaHoc) {
        KhoaHoc khoaHoc = timHoacNem(maKhoaHoc);
        kiemTraQuyenTacGia(khoaHoc, maNguoiDung);
        return khoaHocMapper.sangPhanHoiChiTiet(khoaHoc);
    }
 
    @Override
    @Transactional(readOnly = true)
    public PhanHoiKhoaHoc layChiTietCongKhai(Long maKhoaHoc) {
        KhoaHoc khoaHoc = khoaHocKho.findByMaKhoaHocAndTrangThai(maKhoaHoc, TrangThaiKhoaHoc.CONG_KHAI)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("khóa học công khai", maKhoaHoc));
        return anNoiDungChuaDuocPhep(khoaHocMapper.sangPhanHoiChiTietHienThi(khoaHoc));
    }
 
    /**
     * Endpoint công khai không yêu cầu đăng nhập — chưa có cách kiểm tra
     * "người xem đã mua khóa học chưa" (thuộc bảng quyen_su_dung do module
     * lopHocLMS quản lý, chưa được xây dựng). Để tránh lộ URL video/tài
     * liệu trả phí, mọi bài học có xemThu=false sẽ bị ẩn đường dẫn/metadata,
     * và toàn bộ nội dung quiz (đáp án) luôn bị ẩn ở response công khai.
     */
    private PhanHoiKhoaHoc anNoiDungChuaDuocPhep(PhanHoiKhoaHoc goc) {
        List<PhanHoiChuong> giaoTrinhAn = goc.danhSachChuong().stream()
                .map(chuong -> new PhanHoiChuong(
                        chuong.maChuong(),
                        chuong.tieuDe(),
                        chuong.viTri(),
                        chuong.danhSachBaiHoc().stream().map(this::anBaiHocNeuChuaXemThu).toList()
                ))
                .toList();
 
        return new PhanHoiKhoaHoc(
                goc.maKhoaHoc(), goc.maTacGia(), goc.tenTacGia(), goc.anhDaiDienTacGia(),
                goc.tieuDe(), goc.mieuTa(), goc.anhBia(), goc.maDanhMuc(), goc.tenDanhMuc(),
                goc.gia(), goc.phiDayHoc(), goc.trangThai(), goc.lyDoTuChoi(),
                goc.ngayTao(), goc.ngayCapNhat(), goc.diemTrungBinh(), goc.soLuongDanhGia(),
                giaoTrinhAn
        );
    }
 
    private PhanHoiBaiHoc anBaiHocNeuChuaXemThu(PhanHoiBaiHoc b) {
        // Quiz-taking không thuộc phạm vi xem công khai/xem thử (thuộc module lopHocLMS sau này) -> luôn ẩn đáp án
        if (b.xemThu()) {
            return new PhanHoiBaiHoc(b.maBaiHoc(), b.tieuDe(), b.loai(), b.viTri(),
                    b.duongDan(), b.duoiTep(), b.metadata(), true, null);
        }
        return new PhanHoiBaiHoc(b.maBaiHoc(), b.tieuDe(), b.loai(), b.viTri(), null, null, Map.of(), false, null);
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiDanhSachKhoaHoc> layKhoaHocCuaToi(Long maNguoiDung) {
        return khoaHocKho.findByMaTacGia(maNguoiDung).stream().map(khoaHocMapper::sangPhanHoiDanhSach).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiDanhSachKhoaHoc> layKhoaHocCoTheMoLop(Long maNguoiDung) {
        List<Long> maKhoaHocTacGia = khoaHocKho.findByMaTacGia(maNguoiDung).stream()
                .map(KhoaHoc::getMaKhoaHoc)
                .toList();

        List<Long> maKhoaHocDayHoc = quyenSuDungKho.findByMaNguoiDung(maNguoiDung).stream()
                .filter(QuyenSuDung::conHieuLuc)
                .filter(q -> q.getLoaiGiayPhep() == LoaiGiayPhep.DAY_HOC)
                .map(QuyenSuDung::getMaKhoaHoc)
                .toList();

        List<Long> tatCa = Stream.concat(maKhoaHocTacGia.stream(), maKhoaHocDayHoc.stream())
                .distinct()
                .toList();

        if (tatCa.isEmpty()) {
            return List.of();
        }
        return khoaHocKho.findAllById(tatCa).stream().map(khoaHocMapper::sangPhanHoiDanhSach).toList();
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiDanhSachKhoaHoc> layKhoaHocDaMua(Long maNguoiDung) {
        List<Long> maKhoaHocDaMua = quyenSuDungKho.findByMaNguoiDung(maNguoiDung).stream()
                .filter(QuyenSuDung::conHieuLuc)
                .map(QuyenSuDung::getMaKhoaHoc)
                .distinct()
                .toList();
        if (maKhoaHocDaMua.isEmpty()) {
            return List.of();
        }
        return khoaHocKho.findAllById(maKhoaHocDaMua).stream()
                .map(khoaHocMapper::sangPhanHoiDanhSach)
                .toList();
    }
 
    @Override
    @Transactional(readOnly = true)
    public PhanHoiKhoaHoc layChiTietChoHocVien(Long maNguoiDung, Long maKhoaHoc) {
        if (!quyenSuDungKho.existsByMaNguoiDungAndMaKhoaHoc(maNguoiDung, maKhoaHoc)) {
            throw new NgoaiLeKhongCoQuyen("Bạn chưa sở hữu khóa học này, không thể xem nội dung đầy đủ");
        }
        KhoaHoc khoaHoc = timHoacNem(maKhoaHoc);
        // Đã kiểm tra quyền sở hữu ở trên -> trả về nội dung đầy đủ, không ẩn bài học
        PhanHoiKhoaHoc chiTiet = khoaHocMapper.sangPhanHoiChiTietHienThi(khoaHoc);
        return ganTienDoTuHoc(chiTiet, maNguoiDung);
    }

    /**
     * Chèn tiến độ tự học (hoàn thành + vị trí dừng) vào từng bài học gốc —
     * chỉ dùng cho layChiTietChoHocVien, KHÔNG áp dụng cho các luồng khác
     * (duyệt công khai, giảng viên quản lý nội dung...) vì record là bất
     * biến nên phải dựng lại toàn bộ cây Chương → Bài học.
     */
    private PhanHoiKhoaHoc ganTienDoTuHoc(PhanHoiKhoaHoc goc, Long maNguoiDung) {
        List<Long> tatCaMaBaiHoc = goc.danhSachChuong().stream()
                .flatMap(c -> c.danhSachBaiHoc().stream())
                .map(PhanHoiBaiHoc::maBaiHoc)
                .toList();
        if (tatCaMaBaiHoc.isEmpty()) {
            return goc;
        }

        // FIX (V14): chỉ lấy tiến độ TỰ HỌC (maLopHoc IS NULL) — không lấy
        // nhầm tiến độ học viên đã có ở 1 lớp học nào đó dùng chung khóa học.
        Map<Long, TienDoBaiHoc> tienDoTheoBaiHoc = new HashMap<>();
        for (TienDoBaiHoc t : tienDoBaiHocKho.findByMaHocVienAndMaBaiHocInAndMaLopHocIsNull(maNguoiDung, tatCaMaBaiHoc)) {
            tienDoTheoBaiHoc.put(t.getMaBaiHoc(), t);
        }

        List<PhanHoiChuong> chuongDaGanTienDo = goc.danhSachChuong().stream()
                .map(chuong -> new PhanHoiChuong(
                        chuong.maChuong(), chuong.tieuDe(), chuong.viTri(),
                        chuong.danhSachBaiHoc().stream().map(b -> {
                            TienDoBaiHoc t = tienDoTheoBaiHoc.get(b.maBaiHoc());
                            boolean hoanThanh = t != null && Boolean.TRUE.equals(t.getHoanThanh());
                            Long viTriDung = t != null ? t.getViTriDung() : null;
                            return new PhanHoiBaiHoc(b.maBaiHoc(), b.tieuDe(), b.loai(), b.viTri(),
                                    b.duongDan(), b.duoiTep(), b.metadata(), b.xemThu(), b.baiKiemTra(),
                                    hoanThanh, viTriDung);
                        }).toList()
                ))
                .toList();

        return new PhanHoiKhoaHoc(
                goc.maKhoaHoc(), goc.maTacGia(), goc.tenTacGia(), goc.anhDaiDienTacGia(),
                goc.tieuDe(), goc.mieuTa(), goc.anhBia(), goc.maDanhMuc(), goc.tenDanhMuc(),
                goc.gia(), goc.phiDayHoc(), goc.trangThai(), goc.lyDoTuChoi(),
                goc.ngayTao(), goc.ngayCapNhat(), goc.diemTrungBinh(), goc.soLuongDanhGia(),
                chuongDaGanTienDo);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiDanhSachKhoaHoc> timKiemCongKhai(Long maDanhMuc, String tuKhoa, String caNhanLoc, String dayHocLoc, Pageable pageable) {
        String tuKhoaSach = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        String caNhanLocSach = (caNhanLoc == null || caNhanLoc.isBlank()) ? null : caNhanLoc.trim();
        String dayHocLocSach = (dayHocLoc == null || dayHocLoc.isBlank()) ? null : dayHocLoc.trim();

        // NÂNG CẤP (Full-Text Search): có từ khóa thật -> dùng query xếp hạng
        // theo độ liên quan (ts_rank); không có từ khóa (chỉ duyệt theo danh
        // mục/giá) -> giữ nguyên query JPQL cũ, không cần xếp hạng gì cả.
        if (tuKhoaSach != null) {
            return khoaHocKho.timKiemCongKhaiTheoDoLienQuan(maDanhMuc, tuKhoaSach, caNhanLocSach, dayHocLocSach,
                            TrangThaiKhoaHoc.CONG_KHAI.name(), pageable)
                    .map(khoaHocMapper::sangPhanHoiDanhSach);
        }
        return khoaHocKho.timKiemCongKhai(maDanhMuc, null, caNhanLocSach, dayHocLocSach,
                        TrangThaiKhoaHoc.CONG_KHAI, pageable)
                .map(khoaHocMapper::sangPhanHoiDanhSach);
    }
 
    @Override
    public PhanHoiKhoaHoc guiKiemDuyet(Long maNguoiDung, Long maKhoaHoc) {
        KhoaHoc khoaHoc = timHoacNem(maKhoaHoc);
        kiemTraQuyenTacGia(khoaHoc, maNguoiDung);
 
        if (khoaHoc.getTrangThai() != TrangThaiKhoaHoc.NHAP
                && khoaHoc.getTrangThai() != TrangThaiKhoaHoc.CHO_BO_SUNG) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Chỉ có thể gửi duyệt khóa học đang ở trạng thái Nháp hoặc Chờ bổ sung");
        }
        if (khoaHoc.getDanhSachChuong().isEmpty()) {
            throw new NgoaiLeDuLieuKhongHopLe("Khóa học cần có ít nhất 1 chương trước khi gửi duyệt");
        }
        kiemTraMoiQuizDeuCoCauHoi(khoaHoc);
 
        khoaHoc.setTrangThai(TrangThaiKhoaHoc.CHO_KIEM_DUYET);
        khoaHoc.setLyDoTuChoi(null);
        khoaHoc.danhDauCapNhat();

        // FIX: trước đây gửi duyệt xong không có QTV_NOI_DUNG nào được báo —
        // họ chỉ biết có việc cần xử lý nếu tự vào trang "Chờ duyệt" kiểm tra
        // thủ công. Báo cho toàn bộ QTV_NOI_DUNG đang hoạt động ngay khi có
        // khóa học mới gửi duyệt.
        for (Long maQtv : nguoiDungTraCuuDichVu.layDanhSachMaNguoiDungTheoVaiTro(VaiTro.QTV_NOI_DUNG)) {
            thongBaoDichVu.guiThongBao(maQtv, LoaiThongBao.KHOA_HOC_CHO_DUYET,
                    "Có khóa học mới cần duyệt",
                    "Khóa học \"" + khoaHoc.getTieuDe() + "\" vừa được gửi kiểm duyệt.",
                    Map.of("maKhoaHoc", khoaHoc.getMaKhoaHoc()));
        }

        return khoaHocMapper.sangPhanHoiChiTiet(khoaHoc);
    }
 
    @Override
    public PhanHoiKhoaHoc rutLaiKiemDuyet(Long maNguoiDung, Long maKhoaHoc) {
        KhoaHoc khoaHoc = timHoacNem(maKhoaHoc);
        kiemTraQuyenTacGia(khoaHoc, maNguoiDung);
 
        if (khoaHoc.getTrangThai() != TrangThaiKhoaHoc.CHO_KIEM_DUYET) {
            throw new NgoaiLeDuLieuKhongHopLe("Khóa học hiện không ở trạng thái chờ duyệt");
        }
 
        khoaHoc.setTrangThai(TrangThaiKhoaHoc.NHAP);
        khoaHoc.danhDauCapNhat();
        return khoaHocMapper.sangPhanHoiChiTiet(khoaHoc);
    }
 
    @Override
    public PhanHoiKhoaHoc luuTru(Long maNguoiDung, Long maKhoaHoc) {
        KhoaHoc khoaHoc = timHoacNem(maKhoaHoc);
        kiemTraQuyenTacGia(khoaHoc, maNguoiDung);
 
        if (khoaHoc.getTrangThai() != TrangThaiKhoaHoc.CONG_KHAI) {
            throw new NgoaiLeDuLieuKhongHopLe("Chỉ có thể lưu trữ khóa học đang công khai");
        }
 
        khoaHoc.setTrangThai(TrangThaiKhoaHoc.LUU_TRU);
        khoaHoc.danhDauCapNhat();
        return khoaHocMapper.sangPhanHoiChiTiet(khoaHoc);
    }
 
    @Override
    public PhanHoiKhoaHoc taiCongKhai(Long maNguoiDung, Long maKhoaHoc) {
        KhoaHoc khoaHoc = timHoacNem(maKhoaHoc);
        kiemTraQuyenTacGia(khoaHoc, maNguoiDung);
 
        if (khoaHoc.getTrangThai() != TrangThaiKhoaHoc.LUU_TRU) {
            throw new NgoaiLeDuLieuKhongHopLe("Chỉ có thể tái công khai khóa học đang lưu trữ");
        }
 
        khoaHoc.setTrangThai(TrangThaiKhoaHoc.CONG_KHAI);
        khoaHoc.danhDauCapNhat();
        return khoaHocMapper.sangPhanHoiChiTiet(khoaHoc);
    }
 
    // ---------- Hỗ trợ nội bộ ----------
 
    private KhoaHoc timHoacNem(Long maKhoaHoc) {
        return khoaHocKho.findById(maKhoaHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("khóa học", maKhoaHoc));
    }
 
    private void kiemTraQuyenTacGia(KhoaHoc khoaHoc, Long maNguoiDung) {
        if (!khoaHoc.laTacGia(maNguoiDung)) {
            throw new NgoaiLeKhongCoQuyen("Bạn không phải tác giả của khóa học này");
        }
    }
 

    /**
     * Chặn gửi duyệt nếu có bất kỳ bài học loại QUIZ nào chưa có câu hỏi —
     * trước đây themBaiHoc() cho tạo khung Quiz rỗng (đúng, vì luồng soạn
     * thảo tự nhiên là tạo khung trước rồi thêm câu hỏi sau), nhưng không
     * đâu kiểm tra lại trước khi công khai, có thể lọt Quiz 0 câu hỏi ra
     * Chợ khóa học khiến học viên không thể hoàn thành khóa học.
     */
    private void kiemTraMoiQuizDeuCoCauHoi(KhoaHoc khoaHoc) {
        for (var chuong : khoaHoc.getDanhSachChuong()) {
            for (BaiHoc baiHoc : chuong.getDanhSachBaiHoc()) {
                if (baiHoc.getLoai() != LoaiBaiHoc.QUIZ) {
                    continue;
                }
                Long maBaiKiemTra = baiKiemTraKho.findByBaiHoc_MaBaiHoc(baiHoc.getMaBaiHoc())
                        .map(bkt -> bkt.getMaBaiKiemTra())
                        .orElse(null);
                long soCauHoi = maBaiKiemTra == null ? 0 : cauHoiKho.countByBaiKiemTra_MaBaiKiemTra(maBaiKiemTra);
                if (soCauHoi == 0) {
                    throw new NgoaiLeDuLieuKhongHopLe(
                            "Bài kiểm tra \"" + baiHoc.getTieuDe() + "\" (chương \"" + chuong.getTieuDe()
                                    + "\") chưa có câu hỏi nào. Vui lòng thêm ít nhất 1 câu hỏi trước khi gửi duyệt.");
                }
            }
        }
    }

}