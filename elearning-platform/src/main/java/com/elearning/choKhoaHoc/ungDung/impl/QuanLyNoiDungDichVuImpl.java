package com.elearning.choKhoaHoc.ungDung.impl;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.*;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauCapNhatBaiHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoBaiHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoCauHoi;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoChuong;
import com.elearning.choKhoaHoc.haTang.BaiHocKho;
import com.elearning.choKhoaHoc.haTang.BaiKiemTraKho;
import com.elearning.choKhoaHoc.haTang.CauHoiKho;
import com.elearning.choKhoaHoc.haTang.ChuongKho;
import com.elearning.choKhoaHoc.haTang.KhoaHocKho;
import com.elearning.choKhoaHoc.haTang.boPhanNgoai.AdapterLuuTruHocLieu;
import com.elearning.choKhoaHoc.haTang.boPhanNgoai.KetQuaTaiLenTaiLieu;
import com.elearning.choKhoaHoc.haTang.boPhanNgoai.KetQuaTaiLenVideo;
import com.elearning.choKhoaHoc.mienDomain.*;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.LoaiBaiHoc;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.LoaiCauHoi;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.TrangThaiKhoaHoc;
import com.elearning.choKhoaHoc.ungDung.QuanLyNoiDungDichVu;
import com.elearning.dungChung.cauHoi.QuyTacCauHoi;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.lopHocLMS.haTang.QuyenSuDungKho;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
class QuanLyNoiDungDichVuImpl implements QuanLyNoiDungDichVu {

    private final ChuongKho chuongKho;
    private final BaiHocKho baiHocKho;
    private final BaiKiemTraKho baiKiemTraKho;
    private final CauHoiKho cauHoiKho;
    private final KhoaHocKho khoaHocKho;
    private final AdapterLuuTruHocLieu adapterLuuTruHocLieu;
    private final QuyenSuDungKho quyenSuDungKho;
    private final PlatformTransactionManager transactionManager;

    /**
     * Chạy 1 khối lệnh HOÀN TOÀN KHÔNG có transaction bao quanh (tạm ngưng
     * transaction đang có sẵn của thread hiện tại nếu có, trả lại connection
     * CSDL cho pool trong lúc chạy, rồi khôi phục transaction cũ khi xong).
     *
     * Dùng cho các thao tác I/O CHẬM không đụng gì tới CSDL (upload file lên
     * Cloudinary/R2 — có thể mất vài phút với file lớn/mạng chậm).
     *
     * FIX: bản trước đánh dấu @Transactional(propagation = NOT_SUPPORTED)
     * cho NGUYÊN CẢ HÀM themBaiHoc — tưởng an toàn nhưng lại cắt luôn Session
     * cần cho bước kiemTraQuyenTacGia(chuong.getKhoaHoc(), ...) ở ĐẦU hàm
     * (Chuong.khoaHoc là quan hệ @ManyToOne LAZY thật, không như LopHoc dùng
     * Long thuần), gây lỗi runtime "LazyInitializationException: no Session"
     * ngay khi giảng viên upload video. Sửa lại: chỉ bọc ĐÚNG lệnh upload
     * trong khối không-transaction này, giữ nguyên transaction bình thường
     * (session còn sống) cho phần kiểm tra quyền ở đầu và phần lưu CSDL ở
     * cuối — không cần tự viết 1 method riêng + lo self-invocation bỏ qua
     * proxy AOP của Spring (lỗi kinh điển khi gọi method @Transactional từ
     * chính method khác trong CÙNG class).
     */
    private <T> T chayKhongTransaction(java.util.function.Supplier<T> hanhDong) {
        TransactionTemplate mauKhongGiaoDich = new TransactionTemplate(transactionManager);
        mauKhongGiaoDich.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        return mauKhongGiaoDich.execute(trangThai -> hanhDong.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PhanHoiBaiHoc> layChiTietBaiHoc(Long maBaiHoc) {
        return baiHocKho.findById(maBaiHoc).map(b -> {
            BaiKiemTra bkt = b.getLoai() == LoaiBaiHoc.QUIZ
                    ? baiKiemTraKho.findByBaiHoc_MaBaiHoc(b.getMaBaiHoc()).orElse(null)
                    : null;
            return sangPhanHoiBaiHoc(b, bkt);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiChuong> layGiaoTrinh(Long maKhoaHoc) {
        return chuongKho.findByKhoaHoc_MaKhoaHocOrderByViTriAsc(maKhoaHoc).stream()
                .map(this::sangPhanHoiChuong)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiChuong> layGiaoTrinhHienThi(Long maKhoaHoc) {
        return chuongKho.findByKhoaHoc_MaKhoaHocOrderByViTriAsc(maKhoaHoc).stream()
                .map(this::sangPhanHoiChuongHienThi)
                .toList();
    }

    @Override
    public PhanHoiBaiHoc anBaiHoc(Long maNguoiDung, Long maBaiHoc) {
        return datTrangThaiAn(maNguoiDung, maBaiHoc, true);
    }

    @Override
    public PhanHoiBaiHoc hienBaiHoc(Long maNguoiDung, Long maBaiHoc) {
        return datTrangThaiAn(maNguoiDung, maBaiHoc, false);
    }

    private PhanHoiBaiHoc datTrangThaiAn(Long maNguoiDung, Long maBaiHoc, boolean an) {
        BaiHoc baiHoc = baiHocKho.findById(maBaiHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học", maBaiHoc));
        // CHỦ Ý dùng kiểm tra quyền tác giả KHÔNG khóa theo trạng thái công
        // khai (khác kiemTraQuyenTacGia) — ẩn/hiện bài học chính là thao tác
        // AN TOÀN được thiết kế để dùng ngay khi khóa học đang công khai/đã
        // bán (xem xoaBaiHoc), nên không thể bắt giảng viên phải lưu trữ
        // khóa học trước mới ẩn được bài học.
        if (!baiHoc.getChuong().getKhoaHoc().laTacGia(maNguoiDung)) {
            throw new NgoaiLeKhongCoQuyen("Bạn không phải tác giả của khóa học này");
        }

        baiHoc.datAn(an);

        BaiKiemTra baiKiemTra = baiHoc.getLoai() == LoaiBaiHoc.QUIZ
                ? baiKiemTraKho.findByBaiHoc_MaBaiHoc(maBaiHoc).orElse(null)
                : null;
        return sangPhanHoiBaiHoc(baiHoc, baiKiemTra);
    }

    @Override
    public PhanHoiChuong themChuong(Long maNguoiDung, Long maKhoaHoc, YeuCauTaoChuong yeuCau) {
        KhoaHoc khoaHoc = khoaHocKho.findById(maKhoaHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("khóa học", maKhoaHoc));
        kiemTraQuyenTacGia(khoaHoc, maNguoiDung);

        Chuong chuong = new Chuong(khoaHoc, yeuCau.tieuDe(), yeuCau.viTri());
        chuongKho.save(chuong);
        return sangPhanHoiChuong(chuong);
    }

    @Override
    public PhanHoiChuong capNhatChuong(Long maNguoiDung, Long maChuong, YeuCauTaoChuong yeuCau) {
        Chuong chuong = chuongKho.findById(maChuong)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("chương", maChuong));
        kiemTraQuyenTacGia(chuong.getKhoaHoc(), maNguoiDung);

        chuong.setTieuDe(yeuCau.tieuDe());
        chuong.setViTri(yeuCau.viTri());
        return sangPhanHoiChuong(chuong);
    }

    @Override
    public void xoaChuong(Long maNguoiDung, Long maChuong) {
        Chuong chuong = chuongKho.findById(maChuong)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("chương", maChuong));
        kiemTraQuyenTacGia(chuong.getKhoaHoc(), maNguoiDung);

        if (!chuong.getDanhSachBaiHoc().isEmpty()) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Không thể xóa chương khi bên trong vẫn còn bài học. "
                            + "Vui lòng xóa hết bài học trong chương này trước.");
        }

        chuongKho.delete(chuong);
    }

    @Override
    // FIX: KHÔNG còn @Transactional(propagation = NOT_SUPPORTED) ở đây nữa —
    // xem giải thích chi tiết ở chayKhongTransaction() phía trên. Hàm này giờ
    // dùng lại @Transactional mặc định của class (session/transaction bình
    // thường xuyên suốt), CHỈ đúng đoạn upload file bên dưới mới tạm ngưng
    // transaction qua chayKhongTransaction(...).
    public PhanHoiBaiHoc themBaiHoc(Long maNguoiDung, Long maChuong, YeuCauTaoBaiHoc yeuCau, MultipartFile file) {
        Chuong chuong = chuongKho.findById(maChuong)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("chương", maChuong));
        kiemTraQuyenTacGia(chuong.getKhoaHoc(), maNguoiDung);

        if (yeuCau.tieuDe() == null || yeuCau.tieuDe().isBlank()) {
            throw new NgoaiLeDuLieuKhongHopLe("Tiêu đề bài học không được để trống");
        }
        if (yeuCau.loai() == null) {
            throw new NgoaiLeDuLieuKhongHopLe("Vui lòng chọn loại bài học");
        }

        BaiHoc baiHoc = new BaiHoc(chuong, yeuCau.tieuDe(), yeuCau.loai(), yeuCau.viTri());
        baiHoc.setXemThu(yeuCau.xemThu());

        switch (yeuCau.loai()) {
            case VIDEO -> {
                khongDuocRong(file, "Vui lòng chọn tệp video");
                KetQuaTaiLenVideo kq = chayKhongTransaction(() -> adapterLuuTruHocLieu.taiLenVideo(file));
                baiHoc.setDuongDan(kq.url());
                baiHoc.setDuoiTep(kq.dinhDang());
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("thoiLuongGiay", kq.thoiLuongGiay());
                metadata.put("chieuRong", kq.chieuRong());
                metadata.put("chieuCao", kq.chieuCao());
                baiHoc.setMetadata(metadata);
            }
            case PDF, DOC -> {
                khongDuocRong(file, "Vui lòng chọn tệp tài liệu");
                KetQuaTaiLenTaiLieu kq = chayKhongTransaction(() -> adapterLuuTruHocLieu.taiLenTaiLieu(file));
                baiHoc.setDuongDan(kq.url());
                baiHoc.setDuoiTep(kq.duoiTep());
                baiHoc.setMetadata(Map.of("kichThuocByte", kq.kichThuocByte()));
            }
            case QUIZ -> baiHoc.setDuongDan(""); // ràng buộc NOT NULL ở DB, bài Quiz không có file
        }

        baiHocKho.save(baiHoc);

        BaiKiemTra baiKiemTra = null;
        if (yeuCau.loai() == LoaiBaiHoc.QUIZ) {
            baiKiemTra = new BaiKiemTra(baiHoc, yeuCau.diemToiThieu(), yeuCau.thoiGian());
            baiKiemTraKho.save(baiKiemTra);
        }

        return sangPhanHoiBaiHoc(baiHoc, baiKiemTra);
    }

    @Override
    public PhanHoiBaiHoc capNhatBaiHoc(Long maNguoiDung, Long maBaiHoc, YeuCauCapNhatBaiHoc yeuCau) {
        BaiHoc baiHoc = baiHocKho.findById(maBaiHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học", maBaiHoc));
        kiemTraQuyenTacGia(baiHoc.getChuong().getKhoaHoc(), maNguoiDung);

        baiHoc.setTieuDe(yeuCau.tieuDe());
        baiHoc.setViTri(yeuCau.viTri());
        baiHoc.setXemThu(yeuCau.xemThu());

        BaiKiemTra baiKiemTra = baiHoc.getLoai() == LoaiBaiHoc.QUIZ
                ? baiKiemTraKho.findByBaiHoc_MaBaiHoc(maBaiHoc).orElse(null)
                : null;
        return sangPhanHoiBaiHoc(baiHoc, baiKiemTra);
    }

    @Override
    public void xoaBaiHoc(Long maNguoiDung, Long maBaiHoc) {
        BaiHoc baiHoc = baiHocKho.findById(maBaiHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học", maBaiHoc));
        KhoaHoc khoaHoc = baiHoc.getChuong().getKhoaHoc();
        kiemTraQuyenTacGia(khoaHoc, maNguoiDung);

        // Ràng buộc xóa học liệu: nếu khóa học đã phát sinh giao dịch (đã có
        // học viên sở hữu), chặn xóa vật lý bài học để không làm mất nội dung
        // học viên đã trả tiền — chỉ cho xóa cứng khi khóa học CHƯA có ai sở
        // hữu (ví dụ còn NHÁP hoặc mới CÔNG KHAI nhưng chưa bán được).
        if (quyenSuDungKho.existsByMaKhoaHoc(khoaHoc.getMaKhoaHoc())) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Không thể xóa vật lý bài học vì khóa học đã có học viên sở hữu. "
                            + "Vui lòng chuyển bài học sang chế độ ẩn thay vì xóa để không làm mất "
                            + "quyền truy cập nội dung của học viên đã mua.");
        }

        baiHocKho.delete(baiHoc);

        // FIX: dọn file trên R2 sau khi xóa bản ghi DB thành công — chỉ áp
        // dụng PDF/DOC (video nằm trên Cloudinary, khác hạ tầng, chưa xử lý
        // ở đây). Gọi SAU khi delete() đã chắc chắn thành công, tránh xóa
        // nhầm file của một bài học mà lệnh xóa DB sau đó lại thất bại.
        if (baiHoc.getLoai() == LoaiBaiHoc.PDF || baiHoc.getLoai() == LoaiBaiHoc.DOC) {
            adapterLuuTruHocLieu.xoaTaiLieu(baiHoc.getDuongDan());
        }
    }

    @Override
    public PhanHoiBaiHoc themCauHoi(Long maNguoiDung, Long maBaiHoc, YeuCauTaoCauHoi yeuCau) {
        BaiHoc baiHoc = baiHocKho.findById(maBaiHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học", maBaiHoc));
        kiemTraQuyenTacGia(baiHoc.getChuong().getKhoaHoc(), maNguoiDung);

        if (baiHoc.getLoai() != LoaiBaiHoc.QUIZ) {
            throw new NgoaiLeDuLieuKhongHopLe("Bài học không phải loại Quiz, không thể thêm câu hỏi");
        }
        BaiKiemTra baiKiemTra = baiKiemTraKho.findByBaiHoc_MaBaiHoc(maBaiHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài kiểm tra của bài học", maBaiHoc));

        LoaiCauHoi loaiCauHoi = chuyenDoiLoaiCauHoi(yeuCau.loaiCauHoi());
        // Quy tắc hợp lệ theo từng dạng câu hỏi (chọn đáp án / trả lời ngắn / sắp xếp /
        // ghép cặp) dùng chung với quiz bổ sung — xem QuyTacCauHoi.
        QuyTacCauHoi.kiemTraHopLe(loaiCauHoi.name(), yeuCau.dapAn() == null ? null : yeuCau.dapAn().stream()
                .map(d -> new QuyTacCauHoi.DapAn(d.id(), d.noiDung(), d.noiDungGhep(), d.dapAnDung()))
                .toList());

        List<DapAnCauHoi> dapAn = yeuCau.dapAn().stream()
                .map(d -> new DapAnCauHoi(d.id(), d.noiDung(), d.dapAnDung(), d.noiDungGhep()))
                .toList();

        CauHoi cauHoi = new CauHoi(baiKiemTra, yeuCau.noiDung(), yeuCau.viTri(), loaiCauHoi, dapAn);
        cauHoiKho.save(cauHoi);

        return sangPhanHoiBaiHoc(baiHoc, baiKiemTra);
    }

    @Override
    public void xoaCauHoi(Long maNguoiDung, Long maCauHoi) {
        CauHoi cauHoi = cauHoiKho.findById(maCauHoi)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("câu hỏi", maCauHoi));
        kiemTraQuyenTacGia(cauHoi.getBaiKiemTra().getBaiHoc().getChuong().getKhoaHoc(), maNguoiDung);
        cauHoiKho.delete(cauHoi);
    }

    // ---------- Hỗ trợ nội bộ ----------

    private void kiemTraQuyenTacGia(KhoaHoc khoaHoc, Long maNguoiDung) {
        if (!khoaHoc.laTacGia(maNguoiDung)) {
            throw new NgoaiLeKhongCoQuyen("Bạn không phải tác giả của khóa học này");
        }
        // Khóa quyền chỉnh sửa nội dung khi khóa học đang chờ duyệt HOẶC đang
        // công khai — trước đây chỉ chặn CHO_KIEM_DUYET, bỏ sót CONG_KHAI,
        // khiến giảng viên sửa được nội dung khóa học đang bán cho học viên.
        // Muốn sửa khi đang công khai: gọi luuTru() để chuyển về LUU_TRU,
        // sửa xong gọi taiCongKhai() để công khai lại.
        if (khoaHoc.getTrangThai() == TrangThaiKhoaHoc.CHO_KIEM_DUYET
                || khoaHoc.getTrangThai() == TrangThaiKhoaHoc.CONG_KHAI) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    khoaHoc.getTrangThai() == TrangThaiKhoaHoc.CONG_KHAI
                            ? "Khóa học đang công khai, không thể chỉnh sửa nội dung. Vui lòng lưu trữ khóa học trước."
                            : "Khóa học đang chờ kiểm duyệt, không thể chỉnh sửa nội dung.");
        }
    }

    private void khongDuocRong(MultipartFile file, String thongBao) {
        if (file == null || file.isEmpty()) {
            throw new NgoaiLeDuLieuKhongHopLe(thongBao);
        }
    }

    private LoaiCauHoi chuyenDoiLoaiCauHoi(String giaTri) {
        if (giaTri == null || giaTri.isBlank()) {
            return LoaiCauHoi.MOT_DAP_AN;
        }
        try {
            return LoaiCauHoi.valueOf(giaTri);
        } catch (IllegalArgumentException ex) {
            throw new NgoaiLeDuLieuKhongHopLe("Loại câu hỏi không hợp lệ: " + giaTri);
        }
    }

    private PhanHoiChuong sangPhanHoiChuong(Chuong c) {
        List<PhanHoiBaiHoc> baiHocs = baiHocKho.findByChuong_MaChuongOrderByViTriAsc(c.getMaChuong()).stream()
                .map(b -> {
                    BaiKiemTra bkt = b.getLoai() == LoaiBaiHoc.QUIZ
                            ? baiKiemTraKho.findByBaiHoc_MaBaiHoc(b.getMaBaiHoc()).orElse(null)
                            : null;
                    return sangPhanHoiBaiHoc(b, bkt);
                })
                .toList();
        return new PhanHoiChuong(c.getMaChuong(), c.getTieuDe(), c.getViTri(), baiHocs);
    }

    /**
     * Như sangPhanHoiChuong nhưng bỏ qua các bài học đang ẩn (BaiHoc.isAn())
     * VÀ ẩn cờ đáp án đúng (dapAnDung) của mọi câu hỏi quiz — dùng cho mọi
     * luồng hiển thị cho học viên/khách xem TRƯỚC khi nộp bài (marketplace
     * công khai, chi tiết khóa học tự học). Nếu không ẩn, học viên tự học sẽ
     * thấy sẵn đáp án đúng ngay trên trang giáo trình, coi như hệ thống "tự
     * đánh dấu đáp án" thay vì để họ tự làm rồi mới được chấm (xem
     * KiemTraTrongLopDichVuImpl.layDeBaiTuHoc — nơi ĐÚNG đã ẩn đáp án khi
     * lấy đề để làm bài, nhưng chi tiết khóa học lại lộ qua đường vòng này).
     */
    private PhanHoiChuong sangPhanHoiChuongHienThi(Chuong c) {
        List<PhanHoiBaiHoc> baiHocs = baiHocKho.findByChuong_MaChuongOrderByViTriAsc(c.getMaChuong()).stream()
                .filter(b -> !b.isAn())
                .map(b -> {
                    BaiKiemTra bkt = b.getLoai() == LoaiBaiHoc.QUIZ
                            ? baiKiemTraKho.findByBaiHoc_MaBaiHoc(b.getMaBaiHoc()).orElse(null)
                            : null;
                    return sangPhanHoiBaiHoc(b, bkt, false);
                })
                .toList();
        return new PhanHoiChuong(c.getMaChuong(), c.getTieuDe(), c.getViTri(), baiHocs);
    }

    private PhanHoiBaiHoc sangPhanHoiBaiHoc(BaiHoc b, BaiKiemTra bkt) {
        return sangPhanHoiBaiHoc(b, bkt, true);
    }

    private PhanHoiBaiHoc sangPhanHoiBaiHoc(BaiHoc b, BaiKiemTra bkt, boolean loDapAnDung) {
        PhanHoiBaiKiemTra phanHoiBaiKiemTra = null;
        if (bkt != null) {
            List<PhanHoiCauHoi> cauHois = cauHoiKho.findByBaiKiemTra_MaBaiKiemTraOrderByViTriAsc(bkt.getMaBaiKiemTra())
                    .stream()
                    .map(c -> sangPhanHoiCauHoi(c, loDapAnDung))
                    .toList();
            phanHoiBaiKiemTra = new PhanHoiBaiKiemTra(bkt.getMaBaiKiemTra(), bkt.getDiemToiThieu(), bkt.getThoiGian(),
                    cauHois);
        }
        // Chỉ video mới cần ký URL (DRM cơ bản) — PDF/DOC lưu ở R2 vẫn
        // trả URL công khai R2 như cũ, không đổi gì. Sinh MỚI mỗi lần map (không
        // lưu URL đã ký vào CSDL/cache) để URL luôn còn hạn khi tới tay học
        // viên, dù họ xem giáo trình cách lúc tải video lên rất lâu.
        String duongDanTraVe = b.getLoai() == LoaiBaiHoc.VIDEO
                ? adapterLuuTruHocLieu.layDuongDanVideoCoChuKy(b.getDuongDan())
                : b.getDuongDan();
        return new PhanHoiBaiHoc(
                b.getMaBaiHoc(), b.getTieuDe(), b.getLoai(), b.getViTri(),
                duongDanTraVe, b.getDuoiTep(), b.getMetadata(),
                Boolean.TRUE.equals(b.getXemThu()), phanHoiBaiKiemTra);
    }

    private PhanHoiCauHoi sangPhanHoiCauHoi(CauHoi c, boolean loDapAnDung) {
        List<PhanHoiDapAn> dapAn;
        if (loDapAnDung) {
            dapAn = c.getDapAn().stream()
                    .map(d -> new PhanHoiDapAn(d.id(), d.noiDung(), d.dapAnDung(), d.noiDungGhep()))
                    .toList();
        } else {
            // Đường hiển thị cho học viên/khách: ngoài cờ đúng/sai còn phải che các dạng mà
            // CHÍNH CẤU TRÚC dữ liệu lộ đáp án — TRA_LOI_NGAN (noiDung từng phần tử là đáp án
            // chấp nhận), SAP_XEP (thứ tự lưu là thứ tự đúng), GHEP_CAP (noiDungGhep là vế phải
            // ứng với vế trái). QuyTacCauHoi lo việc này để 2 hệ thống quiz che giống hệt nhau.
            List<QuyTacCauHoi.DapAn> trungLap = c.getDapAn().stream()
                    .map(d -> new QuyTacCauHoi.DapAn(d.id(), d.noiDung(), d.noiDungGhep(),
                            Boolean.TRUE.equals(d.dapAnDung())))
                    .toList();
            dapAn = QuyTacCauHoi.anDapAnChoHocVien(c.getLoaiCauHoi().name(), trungLap).stream()
                    .map(d -> new PhanHoiDapAn(d.id(), d.noiDung(), null, null))
                    .toList();
        }
        return new PhanHoiCauHoi(c.getMaCauHoi(), c.getNoiDung(), c.getViTri(), c.getLoaiCauHoi().name(), dapAn);
    }
}