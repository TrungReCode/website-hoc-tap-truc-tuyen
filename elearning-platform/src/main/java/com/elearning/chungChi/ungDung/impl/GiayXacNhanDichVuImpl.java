package com.elearning.chungChi.ungDung.impl;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiBaiHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiChuong;
import com.elearning.choKhoaHoc.haTang.KhoaHocKho;
import com.elearning.choKhoaHoc.mienDomain.KhoaHoc;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.LoaiBaiHoc;
import com.elearning.choKhoaHoc.ungDung.QuanLyNoiDungDichVu;
import com.elearning.chungChi.giaoDien.dto.phanHoi.PhanHoiGiayXacNhan;
import com.elearning.chungChi.giaoDien.dto.phanHoi.PhanHoiTienDo;
import com.elearning.chungChi.haTang.AdapterLuuTruGiayXacNhan;
import com.elearning.chungChi.haTang.GiayXacNhanCaNhanKho;
import com.elearning.chungChi.haTang.GiayXacNhanLopHocKho;
import com.elearning.chungChi.haTang.TaoFilePdf;
import com.elearning.chungChi.haTang.TaoQrCode;
import com.elearning.chungChi.haTang.TienDoKhoaHocCaNhanKho;
import com.elearning.chungChi.haTang.TienDoLopHocKho;
import com.elearning.chungChi.mienDomain.GiayXacNhanCaNhan;
import com.elearning.chungChi.mienDomain.GiayXacNhanLopHoc;
import com.elearning.chungChi.mienDomain.TienDoKhoaHocCaNhan;
import com.elearning.chungChi.mienDomain.TienDoLopHoc;
import com.elearning.chungChi.ungDung.GiayXacNhanDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.HoSoCongKhaiDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.ThongTinNguoiDungCoBan;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.lopHocLMS.haTang.BaiHocBoSungKho;
import com.elearning.lopHocLMS.haTang.BaiKiemTraBoSungKho;
import com.elearning.lopHocLMS.haTang.CauHinhBaiGocKho;
import com.elearning.lopHocLMS.haTang.KetQuaQuizBoSungKho;
import com.elearning.lopHocLMS.haTang.KetQuaQuizKho;
import com.elearning.lopHocLMS.haTang.LopHocKho;
import com.elearning.lopHocLMS.haTang.QuyenSuDungKho;
import com.elearning.lopHocLMS.haTang.ThanhVienLopKho;
import com.elearning.lopHocLMS.haTang.TienDoBaiBoSungKho;
import com.elearning.lopHocLMS.haTang.TienDoBaiHocKho;
import com.elearning.lopHocLMS.mienDomain.BaiHocBoSung;
import com.elearning.lopHocLMS.mienDomain.BaiKiemTraBoSung;
import com.elearning.lopHocLMS.mienDomain.CauHinhBaiGoc;
import com.elearning.lopHocLMS.mienDomain.KetQuaQuiz;
import com.elearning.lopHocLMS.mienDomain.KetQuaQuizBoSung;
import com.elearning.lopHocLMS.mienDomain.LopHoc;
import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiHocLieuBoSung;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
class GiayXacNhanDichVuImpl implements GiayXacNhanDichVu {

    private static final SecureRandom NGAU_NHIEN = new SecureRandom();
    private static final String BANG_CHU_SO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // bỏ ký tự dễ nhầm (I,O,0,1)

    private final QuyenSuDungKho quyenSuDungKho;
    private final ThanhVienLopKho thanhVienLopKho;
    private final LopHocKho lopHocKho;
    private final KhoaHocKho khoaHocKho;
    private final QuanLyNoiDungDichVu quanLyNoiDungDichVu;
    private final CauHinhBaiGocKho cauHinhBaiGocKho;
    private final BaiHocBoSungKho baiHocBoSungKho;
    private final BaiKiemTraBoSungKho baiKiemTraBoSungKho;
    private final TienDoBaiHocKho tienDoBaiHocKho;
    private final TienDoBaiBoSungKho tienDoBaiBoSungKho;
    private final KetQuaQuizKho ketQuaQuizKho;
    private final KetQuaQuizBoSungKho ketQuaQuizBoSungKho;
    private final HoSoCongKhaiDichVu hoSoCongKhaiDichVu;

    private final TienDoKhoaHocCaNhanKho tienDoKhoaHocCaNhanKho;
    private final TienDoLopHocKho tienDoLopHocKho;
    private final GiayXacNhanCaNhanKho giayXacNhanCaNhanKho;
    private final GiayXacNhanLopHocKho giayXacNhanLopHocKho;

    private final TaoFilePdf taoFilePdf;
    private final TaoQrCode taoQrCode;
    private final AdapterLuuTruGiayXacNhan adapterLuuTruGiayXacNhan;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // ════════════════════════════════════════════════════════════════════
    //  TÍNH TIẾN ĐỘ
    // ════════════════════════════════════════════════════════════════════

    @Override
    public PhanHoiTienDo tinhLaiTienDoCaNhan(Long maHocVien, Long maKhoaHoc) {
        if (!quyenSuDungKho.existsByMaNguoiDungAndMaKhoaHoc(maHocVien, maKhoaHoc)) {
            throw new NgoaiLeKhongCoQuyen("Bạn chưa sở hữu khóa học này");
        }

        List<PhanHoiBaiHoc> tatCa = layGiaoTrinhPhang(maKhoaHoc);
        List<PhanHoiBaiHoc> noiDung = tatCa.stream().filter(b -> b.loai() != LoaiBaiHoc.QUIZ).toList();
        List<PhanHoiBaiHoc> quiz = tatCa.stream()
                .filter(b -> b.loai() == LoaiBaiHoc.QUIZ && b.baiKiemTra() != null).toList();

        int tongBaiHoc = noiDung.size();
        long baiHoanThanh = demBaiHocGocHoanThanh(maHocVien, noiDung, null);

        KetQuaTongHopQuiz kq = tongHopQuizGoc(maHocVien, quiz, null);

        TienDoKhoaHocCaNhan tienDo = tienDoKhoaHocCaNhanKho.findByMaHocVienAndMaKhoaHoc(maHocVien, maKhoaHoc)
                .orElseGet(() -> tienDoKhoaHocCaNhanKho.save(new TienDoKhoaHocCaNhan(maHocVien, maKhoaHoc)));
        tienDo.capNhat(tongBaiHoc, (int) baiHoanThanh, kq.tongQuiz, kq.quizDat);

        return new PhanHoiTienDo(tienDo.getMaTienDo(), maKhoaHoc, "CA_NHAN",
                tienDo.getTongBaiHoc(), tienDo.getBaiHoanThanh(), tienDo.getTongQuiz(), tienDo.getQuizDat(),
                kq.diemTrungBinh(), tienDo.getPhanTramHoanThanh(), tienDo.isDaDuDieuKien());
    }

    @Override
    public PhanHoiTienDo tinhLaiTienDoLop(Long maHocVien, Long maLopHoc) {
        if (!thanhVienLopKho.existsByMaHocVienAndMaLopHoc(maHocVien, maLopHoc)) {
            throw new NgoaiLeKhongCoQuyen("Bạn chưa tham gia lớp học này");
        }
        LopHoc lop = lopHocKho.findById(maLopHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học", maLopHoc));

        // ── Bài gốc còn hiển thị trong lớp (loại trừ bài bị giảng viên ẩn) ──
        List<PhanHoiBaiHoc> gocHienThi;
        if (lop.getMaKhoaHoc() == null) {
            gocHienThi = List.of();
        } else {
            Set<Long> dsMaAn = cauHinhBaiGocKho.findByLopHoc_MaLopHoc(maLopHoc).stream()
                    .filter(CauHinhBaiGoc::getAnBaiHoc)
                    .map(CauHinhBaiGoc::getMaBaiHoc)
                    .collect(Collectors.toSet());
            gocHienThi = layGiaoTrinhPhang(lop.getMaKhoaHoc()).stream()
                    .filter(b -> !dsMaAn.contains(b.maBaiHoc()))
                    .toList();
        }
        List<PhanHoiBaiHoc> noiDungGoc = gocHienThi.stream().filter(b -> b.loai() != LoaiBaiHoc.QUIZ).toList();
        List<PhanHoiBaiHoc> quizGoc = gocHienThi.stream()
                .filter(b -> b.loai() == LoaiBaiHoc.QUIZ && b.baiKiemTra() != null).toList();

        // ── Bài bổ sung đang hiển thị của lớp ────────────────────────────
        List<BaiHocBoSung> boSungHienThi = baiHocBoSungKho.findByLopHoc_MaLopHocOrderByViTriAsc(maLopHoc).stream()
                .filter(b -> Boolean.TRUE.equals(b.getAnHien()))
                .toList();
        List<BaiHocBoSung> noiDungBoSung = boSungHienThi.stream()
                .filter(b -> b.getLoai() != LoaiHocLieuBoSung.QUIZ).toList();
        List<BaiHocBoSung> quizBoSung = boSungHienThi.stream()
                .filter(b -> b.getLoai() == LoaiHocLieuBoSung.QUIZ).toList();

        int tongBaiHoc = noiDungGoc.size() + noiDungBoSung.size();

        long hoanThanhGoc = demBaiHocGocHoanThanh(maHocVien, noiDungGoc, maLopHoc);
        long hoanThanhBoSung = noiDungBoSung.stream()
                .filter(b -> tienDoBaiBoSungKho
                        .findByMaHocVienAndBaiHocBoSung_MaBaiHocBoSung(maHocVien, b.getMaBaiHocBoSung())
                        .map(t -> Boolean.TRUE.equals(t.getHoanThanh())).orElse(false))
                .count();
        long baiHoanThanh = hoanThanhGoc + hoanThanhBoSung;

        KetQuaTongHopQuiz kqGoc = tongHopQuizGoc(maHocVien, quizGoc, maLopHoc);
        KetQuaTongHopQuiz kqBoSung = tongHopQuizBoSung(maHocVien, quizBoSung);

        int tongQuiz = kqGoc.tongQuiz + kqBoSung.tongQuiz;
        int quizDat = kqGoc.quizDat + kqBoSung.quizDat;
        BigDecimal diemTrungBinh = ganDiem(kqGoc, kqBoSung);

        TienDoLopHoc tienDo = tienDoLopHocKho.findByMaHocVienAndMaLopHoc(maHocVien, maLopHoc)
                .orElseGet(() -> tienDoLopHocKho.save(new TienDoLopHoc(maHocVien, maLopHoc)));
        tienDo.capNhat(tongBaiHoc, (int) baiHoanThanh, tongQuiz, quizDat, diemTrungBinh);

        return new PhanHoiTienDo(tienDo.getMaTienDo(), maLopHoc, "LOP_HOC",
                tienDo.getTongBaiHoc(), tienDo.getBaiHoanThanh(), tienDo.getTongQuiz(), tienDo.getQuizDat(),
                tienDo.getDiemTrungBinh(), tienDo.getPhanTramHoanThanh(), tienDo.isDaDuDieuKien());
    }

    // ════════════════════════════════════════════════════════════════════
    //  CẤP GIẤY
    // ════════════════════════════════════════════════════════════════════

    @Override
    public PhanHoiGiayXacNhan capGiayCaNhan(Long maHocVien, Long maKhoaHoc) {
        Optional<GiayXacNhanCaNhan> daCap = giayXacNhanCaNhanKho.findByMaHocVienAndMaKhoaHoc(maHocVien, maKhoaHoc);
        if (daCap.isPresent()) {
            return sangDto(daCap.get());
        }

        PhanHoiTienDo tienDo = tinhLaiTienDoCaNhan(maHocVien, maKhoaHoc);
        if (!tienDo.daDuDieuKien()) {
            throw new NgoaiLeDuLieuKhongHopLe(thongBaoChuaDuDieuKien(tienDo));
        }

        KhoaHoc khoaHoc = khoaHocKho.findById(maKhoaHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("khóa học", maKhoaHoc));
        String hoTenHocVien = tenNguoiDung(maHocVien);
        String maXacThuc = sinhMaXacThucDuyNhat("CN");

        GiayXacNhanCaNhan giay = new GiayXacNhanCaNhan(maXacThuc, maHocVien, maKhoaHoc,
                tienDo.phanTramHoanThanh(), tienDo.diemTrungBinh());

        renderVaLuuFile(giay::setFilePdfUrl, giay::setQrCodeUrl,
                hoTenHocVien, khoaHoc.getTieuDe(), "CA_NHAN", null,
                tienDo.phanTramHoanThanh(), tienDo.diemTrungBinh(), giay.getNgayCap(), maXacThuc,
                "giay-xac-nhan-ca-nhan");

        giayXacNhanCaNhanKho.save(giay);
        return sangDto(giay);
    }

    @Override
    public PhanHoiGiayXacNhan capGiayLopHoc(Long maHocVien, Long maLopHoc) {
        Optional<GiayXacNhanLopHoc> daCap = giayXacNhanLopHocKho.findByMaHocVienAndMaLopHoc(maHocVien, maLopHoc);
        if (daCap.isPresent()) {
            return sangDto(daCap.get());
        }

        // FIX: chặn tường minh — chỉ HỌC VIÊN THẬT (có trong thanh_vien_lop)
        // mới được cấp Giấy xác nhận lớp học, không áp dụng cho giảng viên
        // phụ trách lớp dù họ có tiến độ (đủ điều kiện) hay không. Lớp bảo vệ
        // này độc lập với việc đã chặn giảng viên ghi tiến độ ở nguồn
        // (KiemTraTrongLopDichVuImpl.kiemTraDuocNopBai,
        // TienDoHocDichVuImpl.kiemTraLaThanhVien) — phòng trường hợp tiến độ
        // "ảo" phát sinh từ đường khác trong tương lai vẫn không thể quy đổi
        // thành giấy xác nhận cho giảng viên tự cấp cho chính lớp mình dạy.
        LopHoc lop = lopHocKho.findById(maLopHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học", maLopHoc));
        boolean laHocVienThat = thanhVienLopKho.existsByMaHocVienAndMaLopHoc(maHocVien, maLopHoc);
        if (!laHocVienThat) {
            String lyDo = lop.getMaGiangVien().equals(maHocVien)
                    ? "Bạn là giảng viên phụ trách lớp học này, không phải học viên — không thể tự cấp giấy xác nhận cho chính mình."
                    : "Bạn chưa tham gia lớp học này.";
            throw new NgoaiLeKhongCoQuyen(lyDo);
        }

        PhanHoiTienDo tienDo = tinhLaiTienDoLop(maHocVien, maLopHoc);
        if (!tienDo.daDuDieuKien()) {
            throw new NgoaiLeDuLieuKhongHopLe(thongBaoChuaDuDieuKien(tienDo));
        }

        String hoTenHocVien = tenNguoiDung(maHocVien);
        String hoTenGiangVien = tenNguoiDung(lop.getMaGiangVien());
        String tieuDeKhoaHoc = lop.getMaKhoaHoc() != null
                ? khoaHocKho.findById(lop.getMaKhoaHoc()).map(KhoaHoc::getTieuDe).orElse("")
                : "";
        String tenLop = "Lớp " + lop.getMaLop() + (tieuDeKhoaHoc.isBlank() ? "" : " — " + tieuDeKhoaHoc);
        String maXacThuc = sinhMaXacThucDuyNhat("LH");

        GiayXacNhanLopHoc giay = new GiayXacNhanLopHoc(maXacThuc, maHocVien, maLopHoc,
                tienDo.phanTramHoanThanh(), tienDo.diemTrungBinh());

        renderVaLuuFile(giay::setFilePdfUrl, giay::setQrCodeUrl,
                hoTenHocVien, tenLop, "LOP_HOC", hoTenGiangVien,
                tienDo.phanTramHoanThanh(), tienDo.diemTrungBinh(), giay.getNgayCap(), maXacThuc,
                "giay-xac-nhan-lop-hoc");

        giayXacNhanLopHocKho.save(giay);
        return sangDto(giay);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiGiayXacNhan> layDanhSachGiayCuaHocVien(Long maHocVien) {
        List<PhanHoiGiayXacNhan> ketQua = giayXacNhanCaNhanKho.findByMaHocVien(maHocVien).stream()
                .map(this::sangDto).collect(Collectors.toList());
        giayXacNhanLopHocKho.findByMaHocVien(maHocVien).forEach(g -> ketQua.add(sangDto(g)));
        return ketQua;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PhanHoiGiayXacNhan> xacThuc(String maXacThuc) {
        Optional<PhanHoiGiayXacNhan> caNhan = giayXacNhanCaNhanKho.findByMaXacThuc(maXacThuc).map(this::sangDto);
        if (caNhan.isPresent()) {
            return caNhan;
        }
        return giayXacNhanLopHocKho.findByMaXacThuc(maXacThuc).map(this::sangDto);
    }

    @Override
    @Transactional(readOnly = true)
    public String layUrlFilePdf(String maXacThuc) {
        return xacThuc(maXacThuc)
                .map(PhanHoiGiayXacNhan::filePdfUrl)
                .filter(url -> url != null && !url.isBlank())
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("giấy xác nhận", maXacThuc));
    }

    // ════════════════════════════════════════════════════════════════════
    //  HELPER — dùng chung
    // ════════════════════════════════════════════════════════════════════

    private List<PhanHoiBaiHoc> layGiaoTrinhPhang(Long maKhoaHoc) {
        return quanLyNoiDungDichVu.layGiaoTrinhHienThi(maKhoaHoc).stream()
                .flatMap(c -> c.danhSachBaiHoc().stream())
                .toList();
    }

    /**
     * Dựng thông báo "chưa đủ điều kiện" nêu rõ còn thiếu bao nhiêu bài
     * học/bài kiểm tra cần hoàn thành, theo đúng luồng ngoại lệ của UC.
     */
    private String thongBaoChuaDuDieuKien(PhanHoiTienDo tienDo) {
        int baiConThieu = Math.max(0, tienDo.tongBaiHoc() - tienDo.baiHoanThanh());
        int quizConThieu = Math.max(0, tienDo.tongQuiz() - tienDo.quizDat());

        StringBuilder sb = new StringBuilder("Chưa đủ điều kiện cấp giấy xác nhận — còn thiếu ");
        if (baiConThieu > 0) {
            sb.append(baiConThieu).append(" bài học");
        }
        if (quizConThieu > 0) {
            if (baiConThieu > 0) sb.append(" và ");
            sb.append(quizConThieu).append(" bài kiểm tra");
        }
        if (baiConThieu == 0 && quizConThieu == 0) {
            // Trường hợp hiếm: đã hoàn thành số lượng nhưng điểm quiz chưa đạt
            // (VD: đã làm hết quiz nhưng chưa đạt điểm tối thiểu ở lần gần nhất).
            sb.append("một số bài kiểm tra chưa đạt điểm yêu cầu");
        }
        sb.append(" cần hoàn thành.");
        return sb.toString();
    }

    /**
     * @param maLopHoc null → đếm tiến độ TỰ HỌC (dùng cho Chứng chỉ cá nhân);
     *                 khác null → đếm đúng tiến độ của lớp học đó (dùng cho
     *                 Giấy xác nhận lớp học). FIX (V14): trước đây không phân
     *                 biệt, dẫn đến đếm nhầm tiến độ tự học/lớp khác khi tính
     *                 điều kiện cấp giấy.
     */
    private long demBaiHocGocHoanThanh(Long maHocVien, List<PhanHoiBaiHoc> noiDung, Long maLopHoc) {
        if (noiDung.isEmpty()) {
            return 0;
        }
        List<Long> ids = noiDung.stream().map(PhanHoiBaiHoc::maBaiHoc).toList();
        return maLopHoc == null
                ? tienDoBaiHocKho.countByMaHocVienAndMaBaiHocInAndMaLopHocIsNullAndHoanThanhTrue(maHocVien, ids)
                : tienDoBaiHocKho.countByMaHocVienAndMaBaiHocInAndMaLopHocAndHoanThanhTrue(maHocVien, ids, maLopHoc);
    }

    /**
     * @param maLopHoc null → không giới hạn lớp (dùng cho Chứng chỉ tự học:
     *                 chấp nhận kết quả quiz làm ở BẤT KỲ lớp nào của khóa
     *                 học, vì hệ thống hiện CHƯA có cách tự học làm quiz độc
     *                 lập — ket_qua_lam_bai.ma_lop luôn bắt buộc NOT NULL).
     */
    private KetQuaTongHopQuiz tongHopQuizGoc(Long maHocVien, List<PhanHoiBaiHoc> quiz, Long maLopHoc) {
        int tong = quiz.size();
        int dat = 0;
        List<BigDecimal> diemCaoNhatMoiQuiz = new java.util.ArrayList<>();

        for (PhanHoiBaiHoc b : quiz) {
            Long maBaiKiemTra = b.baiKiemTra().maBaiKiemTra();
            List<KetQuaQuiz> ketQuaList = maLopHoc == null
                    ? ketQuaQuizKho.findByMaNguoiDungAndMaBaiKiemTraOrderByThoiDiemLamDesc(maHocVien, maBaiKiemTra)
                    : ketQuaQuizKho.findByMaNguoiDungAndLopHoc_MaLopHocAndMaBaiKiemTra(maHocVien, maLopHoc, maBaiKiemTra);

            if (ketQuaList.isEmpty()) {
                continue;
            }
            boolean daDat = ketQuaList.stream().anyMatch(k -> Boolean.TRUE.equals(k.getKetQua()));
            if (daDat) {
                dat++;
            }
            int diemCaoNhat = ketQuaList.stream().mapToInt(KetQuaQuiz::getDiem).max().orElse(0);
            diemCaoNhatMoiQuiz.add(BigDecimal.valueOf(diemCaoNhat));
        }
        return new KetQuaTongHopQuiz(tong, dat, diemCaoNhatMoiQuiz);
    }

    private KetQuaTongHopQuiz tongHopQuizBoSung(Long maHocVien, List<BaiHocBoSung> quizBoSung) {
        int tong = 0;
        int dat = 0;
        List<BigDecimal> diemCaoNhatMoiQuiz = new java.util.ArrayList<>();

        for (BaiHocBoSung b : quizBoSung) {
            Optional<BaiKiemTraBoSung> bkt = baiKiemTraBoSungKho.findByBaiBoSung_MaBaiHocBoSung(b.getMaBaiHocBoSung());
            if (bkt.isEmpty()) {
                continue; // bài loại QUIZ nhưng chưa cấu hình câu hỏi — bỏ qua, không tính vào tổng
            }
            tong++;
            List<KetQuaQuizBoSung> ketQuaList = ketQuaQuizBoSungKho
                    .findByMaNguoiDungAndBaiKiemTraBoSung_MaBaiKiemTraBoSungOrderByThoiDiemLamDesc(
                            maHocVien, bkt.get().getMaBaiKiemTraBoSung());
            if (ketQuaList.isEmpty()) {
                continue;
            }
            boolean daDat = ketQuaList.stream().anyMatch(k -> Boolean.TRUE.equals(k.getKetQua()));
            if (daDat) {
                dat++;
            }
            int diemCaoNhat = ketQuaList.stream().mapToInt(KetQuaQuizBoSung::getDiem).max().orElse(0);
            diemCaoNhatMoiQuiz.add(BigDecimal.valueOf(diemCaoNhat));
        }
        return new KetQuaTongHopQuiz(tong, dat, diemCaoNhatMoiQuiz);
    }

    private BigDecimal ganDiem(KetQuaTongHopQuiz a, KetQuaTongHopQuiz b) {
        List<BigDecimal> tatCa = new java.util.ArrayList<>(a.diemCacQuiz);
        tatCa.addAll(b.diemCacQuiz);
        if (tatCa.isEmpty()) {
            return null;
        }
        BigDecimal tong = tatCa.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return tong.divide(BigDecimal.valueOf(tatCa.size()), 2, RoundingMode.HALF_UP);
    }

    /** Gói kết quả trung gian khi tổng hợp quiz — không phải Entity, chỉ dùng nội bộ tính toán. */
    private record KetQuaTongHopQuiz(int tongQuiz, int quizDat, List<BigDecimal> diemCacQuiz) {
        BigDecimal diemTrungBinh() {
            if (diemCacQuiz.isEmpty()) {
                return null;
            }
            BigDecimal tong = diemCacQuiz.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            return tong.divide(BigDecimal.valueOf(diemCacQuiz.size()), 2, RoundingMode.HALF_UP);
        }
    }

    private String tenNguoiDung(Long maNguoiDung) {
        return hoSoCongKhaiDichVu.layThongTinCoBan(maNguoiDung)
                .map(ThongTinNguoiDungCoBan::hoTen)
                .orElse("Người dùng #" + maNguoiDung);
    }

    private String sinhMaXacThucDuyNhat(String tienTo) {
        String ma;
        int soLanThu = 0;
        do {
            ma = tienTo + "-" + java.time.Year.now().getValue() + "-" + chuoiNgauNhien(8);
            soLanThu++;
            if (soLanThu > 10) {
                throw new IllegalStateException("Không sinh được mã xác thực duy nhất sau 10 lần thử");
            }
        } while (giayXacNhanCaNhanKho.findByMaXacThuc(ma).isPresent()
                || giayXacNhanLopHocKho.findByMaXacThuc(ma).isPresent());
        return ma;
    }

    private String chuoiNgauNhien(int doDai) {
        StringBuilder sb = new StringBuilder(doDai);
        for (int i = 0; i < doDai; i++) {
            sb.append(BANG_CHU_SO.charAt(NGAU_NHIEN.nextInt(BANG_CHU_SO.length())));
        }
        return sb.toString();
    }

    /**
     * Render PDF + QR rồi tải lên R2 — dùng chung cho cả 2 loại giấy.
     * setterPdfUrl/setterQrUrl là method reference (giay::setFilePdfUrl...)
     * để không cần biết trước kiểu Entity cụ thể ở đây.
     */
    private void renderVaLuuFile(
            java.util.function.Consumer<String> setterPdfUrl, java.util.function.Consumer<String> setterQrUrl,
            String hoTenHocVien, String tieuDeTaiNguyen, String loai, String hoTenGiangVien,
            BigDecimal phanTramHoanThanh, BigDecimal diemTrungBinh,
            java.time.LocalDateTime ngayCap, String maXacThuc, String thuMucCon) {
        try {
            String urlXacThuc = frontendUrl + "/xac-thuc-giay/" + maXacThuc;
            byte[] qrBytes = taoQrCode.tao(urlXacThuc);
            byte[] pdfBytes = taoFilePdf.taoGiayXacNhan(hoTenHocVien, tieuDeTaiNguyen, loai, hoTenGiangVien,
                    phanTramHoanThanh, diemTrungBinh, ngayCap, maXacThuc, qrBytes);

            String urlPdf = adapterLuuTruGiayXacNhan.taiLen(pdfBytes, maXacThuc + ".pdf",
                    "application/pdf", thuMucCon);
            String urlQr = adapterLuuTruGiayXacNhan.taiLen(qrBytes, maXacThuc + "_qr.png",
                    "image/png", thuMucCon + "/qr");

            setterPdfUrl.accept(urlPdf);
            setterQrUrl.accept(urlQr);
        } catch (Exception ex) {
            throw new com.elearning.dungChung.ngoaiLe.NgoaiLeNghiepVu(
                    "Tạo file giấy xác nhận thất bại: " + ex.getMessage(),
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "LOI_TAO_GIAY_XAC_NHAN");
        }
    }

    private PhanHoiGiayXacNhan sangDto(GiayXacNhanCaNhan g) {
        return new PhanHoiGiayXacNhan(g.getMaGiay(), g.getMaXacThuc(), g.getMaHocVien(), g.getMaKhoaHoc(),
                "CA_NHAN", g.getPhanTramHoanThanh(), g.getDiemTrungBinh(), g.getNgayCap(),
                g.getFilePdfUrl(), g.getQrCodeUrl(), g.getTrangThai());
    }

    private PhanHoiGiayXacNhan sangDto(GiayXacNhanLopHoc g) {
        return new PhanHoiGiayXacNhan(g.getMaGiay(), g.getMaXacThuc(), g.getMaHocVien(), g.getMaLopHoc(),
                "LOP_HOC", g.getPhanTramHoanThanh(), g.getDiemTrungBinh(), g.getNgayCap(),
                g.getFilePdfUrl(), g.getQrCodeUrl(), g.getTrangThai());
    }
}