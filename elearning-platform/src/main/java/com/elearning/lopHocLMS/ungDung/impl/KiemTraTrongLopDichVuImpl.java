package com.elearning.lopHocLMS.ungDung.impl;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiBaiHoc;
import com.elearning.choKhoaHoc.haTang.BaiHocKho;
import com.elearning.choKhoaHoc.mienDomain.BaiHoc;
import com.elearning.choKhoaHoc.ungDung.QuanLyNoiDungDichVu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.*;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCauTraLoi;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauNopBaiKiemTra;
import com.elearning.lopHocLMS.haTang.*;
import com.elearning.lopHocLMS.mienDomain.*;
import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiHocLieuBoSung;
import com.elearning.lopHocLMS.ungDung.KiemTraTrongLopDichVu;
import com.elearning.lopHocLMS.ungDung.TienDoHocDichVu;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
class KiemTraTrongLopDichVuImpl implements KiemTraTrongLopDichVu {

        private static final Logger LOG = LoggerFactory.getLogger(KiemTraTrongLopDichVuImpl.class);

        private final LopHocKho lopHocKho;
        private final ThanhVienLopKho thanhVienLopKho;
        private final BaiHocBoSungKho baiHocBoSungKho;
        private final BaiKiemTraBoSungKho baiKiemTraBoSungKho;
        private final CauHoiBoSungKho cauHoiBoSungKho;
        private final KetQuaQuizKho ketQuaQuizKho;
        private final KetQuaQuizBoSungKho ketQuaQuizBoSungKho;
        private final QuanLyNoiDungDichVu quanLyNoiDungDichVu;
        private final TienDoHocDichVu tienDoHocDichVu;
        private final QuyenSuDungKho quyenSuDungKho;
        private final BaiHocKho baiHocKho;
        private final RedisTemplate<String, Object> redisTemplate;

        @Override
        @Transactional(readOnly = true)
        public PhanHoiDeKiemTra layDeBaiGoc(Long maNguoiDung, Long maLopHoc, Long maBaiHoc) {
                kiemTraLaThanhVien(maNguoiDung, maLopHoc);
                PhanHoiBaiHoc baiHoc = quanLyNoiDungDichVu.layChiTietBaiHoc(maBaiHoc)
                                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học", maBaiHoc));

                if (baiHoc.baiKiemTra() == null) {
                        throw new NgoaiLeDuLieuKhongHopLe("Bài học này không phải loại Quiz");
                }
                var bkt = baiHoc.baiKiemTra();
                List<PhanHoiCauHoiTrongLop> dsCauHoi = bkt.danhSachCauHoi().stream()
                                .map(c -> new PhanHoiCauHoiTrongLop(
                                                c.maCauHoi(), c.noiDung(), c.viTri(), c.loaiCauHoi(),
                                                c.dapAn().stream().map(d -> new PhanHoiDapAnAn(d.id(), d.noiDung()))
                                                                .toList()))
                                .toList();

                return new PhanHoiDeKiemTra("GOC", bkt.maBaiKiemTra(), baiHoc.tieuDe(),
                                bkt.diemToiThieu(), bkt.thoiGian(),
                                tinhGiayConLai(maNguoiDung, "GOC", bkt.maBaiKiemTra(), bkt.thoiGian()), dsCauHoi);
        }

        @Override
        @Transactional(readOnly = true)
        public PhanHoiDeKiemTra layDeBaiTuHoc(Long maNguoiDung, Long maBaiHoc) {
                kiemTraQuyenTuHoc(maNguoiDung, maBaiHoc);
                PhanHoiBaiHoc baiHoc = quanLyNoiDungDichVu.layChiTietBaiHoc(maBaiHoc)
                                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học", maBaiHoc));
                if (baiHoc.baiKiemTra() == null) {
                        throw new NgoaiLeDuLieuKhongHopLe("Bài học này không phải loại Quiz");
                }
                var bkt = baiHoc.baiKiemTra();
                List<PhanHoiCauHoiTrongLop> dsCauHoi = bkt.danhSachCauHoi().stream()
                                .map(c -> new PhanHoiCauHoiTrongLop(
                                                c.maCauHoi(), c.noiDung(), c.viTri(), c.loaiCauHoi(),
                                                c.dapAn().stream().map(d -> new PhanHoiDapAnAn(d.id(), d.noiDung()))
                                                                .toList()))
                                .toList();

                return new PhanHoiDeKiemTra("GOC", bkt.maBaiKiemTra(), baiHoc.tieuDe(),
                                bkt.diemToiThieu(), bkt.thoiGian(),
                                tinhGiayConLai(maNguoiDung, "GOC", bkt.maBaiKiemTra(), bkt.thoiGian()), dsCauHoi);
        }

        @Override
        @Transactional(readOnly = true)
        public PhanHoiDeKiemTra layDeBoSung(Long maNguoiDung, Long maLopHoc, Long maBaiHocBoSung) {
                kiemTraLaThanhVien(maNguoiDung, maLopHoc);
                BaiHocBoSung b = baiHocBoSungKho.findById(maBaiHocBoSung)
                                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học bổ sung", maBaiHocBoSung));
                if (b.getLoai() != LoaiHocLieuBoSung.QUIZ) {
                        throw new NgoaiLeDuLieuKhongHopLe("Bài học bổ sung này không phải loại Quiz");
                }
                BaiKiemTraBoSung bkt = baiKiemTraBoSungKho.findByBaiBoSung_MaBaiHocBoSung(maBaiHocBoSung)
                                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("đề kiểm tra bổ sung",
                                                maBaiHocBoSung));

                List<PhanHoiCauHoiTrongLop> dsCauHoi = cauHoiBoSungKho
                                .findByBaiKiemTraBoSung_MaBaiKiemTraBoSungOrderByViTriAsc(bkt.getMaBaiKiemTraBoSung())
                                .stream()
                                .map(c -> new PhanHoiCauHoiTrongLop(
                                                c.getMaCauHoiBaiBoSung(), c.getNoiDung(), c.getViTri(),
                                                c.getLoaiCauHoi().name(),
                                                c.getDapAn().stream().map(d -> new PhanHoiDapAnAn(d.id(), d.noiDung()))
                                                                .toList()))
                                .toList();

                return new PhanHoiDeKiemTra("BO_SUNG", bkt.getMaBaiKiemTraBoSung(), b.getTieuDe(),
                                bkt.getDiemToiThieu(), bkt.getThoiGian(),
                                tinhGiayConLai(maNguoiDung, "BO_SUNG", bkt.getMaBaiKiemTraBoSung(), bkt.getThoiGian()),
                                dsCauHoi);
        }

        @Override
        public PhanHoiKetQuaQuiz nopBaiGoc(Long maNguoiDung, Long maLopHoc, Long maBaiHoc, YeuCauNopBaiKiemTra yeuCau) {
                kiemTraDuocNopBai(maNguoiDung, maLopHoc);
                PhanHoiBaiHoc baiHoc = quanLyNoiDungDichVu.layChiTietBaiHoc(maBaiHoc)
                                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học", maBaiHoc));
                if (baiHoc.baiKiemTra() == null) {
                        throw new NgoaiLeDuLieuKhongHopLe("Bài học này không có đề kiểm tra");
                }
                var bkt = baiHoc.baiKiemTra();
                LopHoc lop = lopHocKho.findById(maLopHoc).orElseThrow();

                var ketQuaCham = chamDiemGoc(bkt, yeuCau.cauTraLoi());
                int diem = ketQuaCham.diem();
                boolean dat = diem >= bkt.diemToiThieu();

                KetQuaQuiz kq = new KetQuaQuiz(maNguoiDung, lop, bkt.maBaiKiemTra(), diem, dat, ketQuaCham.chiTiet());
                ketQuaQuizKho.save(kq);
                xoaThoiGianBatDauQuiz(maNguoiDung, "GOC", bkt.maBaiKiemTra());

                if (dat) {
                        var ycTienDo = new com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCapNhatTienDo(null, true);
                        tienDoHocDichVu.capNhatTienDoBaiGoc(maNguoiDung, maLopHoc, maBaiHoc, ycTienDo);
                }
                return sangPhanHoiKetQua(kq.getMaKetQua(), diem, dat, bkt.diemToiThieu(), kq.getThoiDiemLam(),
                                ketQuaCham.chiTietPhoi());
        }

        @Override
        public PhanHoiKetQuaQuiz nopBaiTuHoc(Long maNguoiDung, Long maBaiHoc, YeuCauNopBaiKiemTra yeuCau) {
                kiemTraQuyenTuHoc(maNguoiDung, maBaiHoc);
                PhanHoiBaiHoc baiHoc = quanLyNoiDungDichVu.layChiTietBaiHoc(maBaiHoc)
                                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học", maBaiHoc));
                if (baiHoc.baiKiemTra() == null) {
                        throw new NgoaiLeDuLieuKhongHopLe("Bài học này không có đề kiểm tra");
                }
                var bkt = baiHoc.baiKiemTra();

                var ketQuaCham = chamDiemGoc(bkt, yeuCau.cauTraLoi());
                int diem = ketQuaCham.diem();
                boolean dat = diem >= bkt.diemToiThieu();

                // lopHoc = null → làm bài khi TỰ HỌC (xem V15/KetQuaQuiz)
                KetQuaQuiz kq = new KetQuaQuiz(maNguoiDung, null, bkt.maBaiKiemTra(), diem, dat, ketQuaCham.chiTiet());
                ketQuaQuizKho.save(kq);
                xoaThoiGianBatDauQuiz(maNguoiDung, "GOC", bkt.maBaiKiemTra());

                if (dat) {
                        var ycTienDo = new com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCapNhatTienDo(null, true);
                        tienDoHocDichVu.capNhatTienDoCaNhan(maNguoiDung, maBaiHoc, ycTienDo);
                }
                return sangPhanHoiKetQua(kq.getMaKetQua(), diem, dat, bkt.diemToiThieu(), kq.getThoiDiemLam(),
                                ketQuaCham.chiTietPhoi());
        }

        @Override
        public PhanHoiKetQuaQuiz nopBaiBoSung(Long maNguoiDung, Long maLopHoc, Long maBaiHocBoSung,
                        YeuCauNopBaiKiemTra yeuCau) {
                kiemTraDuocNopBai(maNguoiDung, maLopHoc);
                BaiKiemTraBoSung bkt = baiKiemTraBoSungKho.findByBaiBoSung_MaBaiHocBoSung(maBaiHocBoSung)
                                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("đề kiểm tra bổ sung",
                                                maBaiHocBoSung));
                LopHoc lop = lopHocKho.findById(maLopHoc).orElseThrow();

                List<CauHoiBoSung> dsCauHoi = cauHoiBoSungKho
                                .findByBaiKiemTraBoSung_MaBaiKiemTraBoSungOrderByViTriAsc(bkt.getMaBaiKiemTraBoSung());

                var ketQuaCham = chamDiemBoSung(dsCauHoi, yeuCau.cauTraLoi(), bkt);
                int diem = ketQuaCham.diem();
                boolean dat = diem >= bkt.getDiemToiThieu();

                KetQuaQuizBoSung kq = new KetQuaQuizBoSung(maNguoiDung, lop, bkt, diem, dat, ketQuaCham.chiTiet());
                ketQuaQuizBoSungKho.save(kq);
                xoaThoiGianBatDauQuiz(maNguoiDung, "BO_SUNG", bkt.getMaBaiKiemTraBoSung());

                if (dat) {
                        var ycTienDo = new com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCapNhatTienDo(null, true);
                        tienDoHocDichVu.capNhatTienDoBaiBoSung(maNguoiDung, maBaiHocBoSung, ycTienDo);
                }
                return sangPhanHoiKetQua(kq.getMaKetQuaBoSung(), diem, dat, bkt.getDiemToiThieu(), kq.getThoiDiemLam(),
                                ketQuaCham.chiTietPhoi());
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<PhanHoiKetQuaQuiz> layKetQuaGanNhatGoc(Long maNguoiDung, Long maBaiHoc) {
                PhanHoiBaiHoc baiHoc = quanLyNoiDungDichVu.layChiTietBaiHoc(maBaiHoc).orElse(null);
                if (baiHoc == null || baiHoc.baiKiemTra() == null)
                        return Optional.empty();
                return ketQuaQuizKho
                                .findByMaNguoiDungAndMaBaiKiemTraOrderByThoiDiemLamDesc(maNguoiDung,
                                                baiHoc.baiKiemTra().maBaiKiemTra())
                                .stream().findFirst()
                                .map(kq -> sangPhanHoiKetQuaTuLuu(kq.getMaKetQua(), kq.getDiem(), kq.getKetQua(),
                                                baiHoc.baiKiemTra().diemToiThieu(), kq.getThoiDiemLam(),
                                                kq.getChiTietDapAn(), baiHoc.baiKiemTra()));
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<PhanHoiKetQuaQuiz> layKetQuaGanNhatBoSung(Long maNguoiDung, Long maBaiHocBoSung) {
                BaiKiemTraBoSung bkt = baiKiemTraBoSungKho.findByBaiBoSung_MaBaiHocBoSung(maBaiHocBoSung).orElse(null);
                if (bkt == null)
                        return Optional.empty();
                List<CauHoiBoSung> dsCauHoi = cauHoiBoSungKho
                                .findByBaiKiemTraBoSung_MaBaiKiemTraBoSungOrderByViTriAsc(bkt.getMaBaiKiemTraBoSung());
                return ketQuaQuizBoSungKho
                                .findByMaNguoiDungAndBaiKiemTraBoSung_MaBaiKiemTraBoSungOrderByThoiDiemLamDesc(
                                                maNguoiDung, bkt.getMaBaiKiemTraBoSung())
                                .stream().findFirst()
                                .map(kq -> sangPhanHoiKetQuaTuLuuBoSung(kq.getMaKetQuaBoSung(), kq.getDiem(),
                                                kq.getKetQua(),
                                                bkt.getDiemToiThieu(), kq.getThoiDiemLam(), kq.getChiTietDapAn(),
                                                dsCauHoi));
        }

        // ---------- Chấm điểm tự động ----------

        private record KetQuaCham(int diem, List<ChiTietDapAnDaChon> chiTiet, List<PhanHoiCauHoiDaCham> chiTietPhoi) {
        }

        private KetQuaCham chamDiemGoc(com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiBaiKiemTra bkt,
                        List<YeuCauCauTraLoi> cauTraLoi) {
                Map<Long, List<Integer>> traLoiMap = cauTraLoi.stream()
                                .collect(java.util.stream.Collectors.toMap(YeuCauCauTraLoi::maCauHoi,
                                                c -> c.dsMaDapAnDaChon() != null ? c.dsMaDapAnDaChon() : List.of()));

                List<ChiTietDapAnDaChon> chiTiet = new ArrayList<>();
                List<PhanHoiCauHoiDaCham> chiTietPhoi = new ArrayList<>();
                int soDung = 0;

                for (var cauHoi : bkt.danhSachCauHoi()) {
                        List<Integer> daDaChon = traLoiMap.getOrDefault(cauHoi.maCauHoi(), List.of());
                        Set<Integer> dapAnDungId = cauHoi.dapAn().stream()
                                        .filter(d -> Boolean.TRUE.equals(d.dapAnDung())).map(d -> d.id())
                                        .collect(java.util.stream.Collectors.toSet());
                        boolean dungCau = new HashSet<>(daDaChon).equals(dapAnDungId);
                        if (dungCau)
                                soDung++;

                        chiTiet.add(new ChiTietDapAnDaChon(cauHoi.maCauHoi(), daDaChon, dungCau));
                        chiTietPhoi.add(new PhanHoiCauHoiDaCham(cauHoi.maCauHoi(), cauHoi.noiDung(),
                                        cauHoi.dapAn().stream()
                                                        .map(d -> new PhanHoiDapAn(d.id(), d.noiDung(), d.dapAnDung()))
                                                        .toList(),
                                        daDaChon, dungCau));
                }
                int tongSo = bkt.danhSachCauHoi().size();
                int diem = tongSo == 0 ? 0 : (int) Math.round(soDung * 100.0 / tongSo);
                return new KetQuaCham(diem, chiTiet, chiTietPhoi);
        }

        private KetQuaCham chamDiemBoSung(List<CauHoiBoSung> dsCauHoi, List<YeuCauCauTraLoi> cauTraLoi,
                        BaiKiemTraBoSung bkt) {
                Map<Long, List<Integer>> traLoiMap = cauTraLoi.stream()
                                .collect(java.util.stream.Collectors.toMap(YeuCauCauTraLoi::maCauHoi,
                                                c -> c.dsMaDapAnDaChon() != null ? c.dsMaDapAnDaChon() : List.of()));

                List<ChiTietDapAnDaChon> chiTiet = new ArrayList<>();
                List<PhanHoiCauHoiDaCham> chiTietPhoi = new ArrayList<>();
                int soDung = 0;

                for (CauHoiBoSung cauHoi : dsCauHoi) {
                        List<Integer> daDaChon = traLoiMap.getOrDefault(cauHoi.getMaCauHoiBaiBoSung(), List.of());
                        Set<Integer> dapAnDungId = cauHoi.getDapAn().stream()
                                        .filter(d -> Boolean.TRUE.equals(d.dapAnDung())).map(DapAnBoSung::id)
                                        .collect(java.util.stream.Collectors.toSet());
                        boolean dungCau = new HashSet<>(daDaChon).equals(dapAnDungId);
                        if (dungCau)
                                soDung++;

                        chiTiet.add(new ChiTietDapAnDaChon(cauHoi.getMaCauHoiBaiBoSung(), daDaChon, dungCau));
                        chiTietPhoi.add(new PhanHoiCauHoiDaCham(cauHoi.getMaCauHoiBaiBoSung(), cauHoi.getNoiDung(),
                                        cauHoi.getDapAn().stream()
                                                        .map(d -> new PhanHoiDapAn(d.id(), d.noiDung(), d.dapAnDung()))
                                                        .toList(),
                                        daDaChon, dungCau));
                }
                int tongSo = dsCauHoi.size();
                int diem = tongSo == 0 ? 0 : (int) Math.round(soDung * 100.0 / tongSo);
                return new KetQuaCham(diem, chiTiet, chiTietPhoi);
        }

        private PhanHoiKetQuaQuiz sangPhanHoiKetQua(Long maKetQua, int diem, boolean dat, int diemToiThieu,
                        java.time.LocalDateTime thoiDiem, List<PhanHoiCauHoiDaCham> chiTiet) {
                return new PhanHoiKetQuaQuiz(maKetQua, diem, dat, diemToiThieu, thoiDiem, chiTiet);
        }

        private PhanHoiKetQuaQuiz sangPhanHoiKetQuaTuLuu(Long maKetQua, int diem, boolean dat, int diemToiThieu,
                        java.time.LocalDateTime thoiDiem, List<ChiTietDapAnDaChon> chiTietLuu,
                        com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiBaiKiemTra bkt) {
                Map<Long, com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiCauHoi> cauHoiMap = bkt.danhSachCauHoi()
                                .stream()
                                .collect(java.util.stream.Collectors.toMap(c -> c.maCauHoi(), c -> c));
                List<PhanHoiCauHoiDaCham> chiTiet = chiTietLuu.stream().map(ct -> {
                        var cauHoi = cauHoiMap.get(ct.maCauHoi());
                        return new PhanHoiCauHoiDaCham(ct.maCauHoi(),
                                        cauHoi != null ? cauHoi.noiDung() : "—",
                                        cauHoi != null ? cauHoi.dapAn().stream()
                                                        .map(d -> new PhanHoiDapAn(d.id(), d.noiDung(), d.dapAnDung()))
                                                        .toList() : List.of(),
                                        ct.dsMaDapAnDaChon(), ct.dapAnDung());
                }).toList();
                return new PhanHoiKetQuaQuiz(maKetQua, diem, dat, diemToiThieu, thoiDiem, chiTiet);
        }

        private PhanHoiKetQuaQuiz sangPhanHoiKetQuaTuLuuBoSung(Long maKetQua, int diem, boolean dat, int diemToiThieu,
                        java.time.LocalDateTime thoiDiem, List<ChiTietDapAnDaChon> chiTietLuu,
                        List<CauHoiBoSung> dsCauHoi) {
                Map<Long, CauHoiBoSung> cauHoiMap = dsCauHoi.stream()
                                .collect(java.util.stream.Collectors.toMap(CauHoiBoSung::getMaCauHoiBaiBoSung, c -> c));
                List<PhanHoiCauHoiDaCham> chiTiet = chiTietLuu.stream().map(ct -> {
                        var cauHoi = cauHoiMap.get(ct.maCauHoi());
                        return new PhanHoiCauHoiDaCham(ct.maCauHoi(),
                                        cauHoi != null ? cauHoi.getNoiDung() : "—",
                                        cauHoi != null ? cauHoi.getDapAn().stream()
                                                        .map(d -> new PhanHoiDapAn(d.id(), d.noiDung(), d.dapAnDung()))
                                                        .toList() : List.of(),
                                        ct.dsMaDapAnDaChon(), ct.dapAnDung());
                }).toList();
                return new PhanHoiKetQuaQuiz(maKetQua, diem, dat, diemToiThieu, thoiDiem, chiTiet);
        }

        /**
         * Tính số giây CÒN LẠI của 1 lượt làm bài có giới hạn thời gian, dựa
         * trên thời điểm bắt đầu lưu ở Redis (KHÔNG phải state phía client) —
         * fix lỗi đồng hồ đếm ngược bị reset về full thời gian mỗi khi học
         * viên thoát ra vào lại trang làm bài (trước đây frontend tự tính
         * giayConLai = thoiGian*60 mỗi lần mount component).
         *
         * Lần đầu gọi (chưa có key Redis): ghi nhận "bây giờ" là thời điểm
         * bắt đầu, TTL đúng bằng thời gian cho phép — trả về đủ thời gian.
         * Các lần gọi sau (đã có key, tức đang làm dở): trừ đi thời gian
         * thực đã trôi qua kể từ lúc bắt đầu.
         */
        private Integer tinhGiayConLai(Long maNguoiDung, String loaiNguon, Long maBaiKiemTra, Integer thoiGianPhut) {
                if (thoiGianPhut == null) {
                        return null; // không giới hạn thời gian
                }
                long tongGiay = thoiGianPhut * 60L;
                try {
                        String khoa = "quiz-batdau:" + maNguoiDung + ":" + loaiNguon + ":" + maBaiKiemTra;
                        long baySo = System.currentTimeMillis() / 1000;
                        Object gt = redisTemplate.opsForValue().get(khoa);
                        long batDau;
                        if (gt == null) {
                                batDau = baySo;
                                redisTemplate.opsForValue().set(khoa, String.valueOf(batDau),
                                                java.time.Duration.ofSeconds(tongGiay));
                        } else {
                                batDau = Long.parseLong(String.valueOf(gt));
                        }
                        long conLai = tongGiay - (baySo - batDau);
                        return (int) Math.max(0, conLai);
                } catch (Exception ex) {
                        // Redis trục trặc (mất kết nối, timeout, giá trị lưu bị hỏng...)
                        // KHÔNG được để làm sập cả API lấy đề bài — học viên vẫn phải
                        // làm bài được. Chấp nhận đánh đổi: mất khả năng chống "câu
                        // giờ" (đồng hồ coi như bắt đầu lại từ đầu) trong đúng lần gọi
                        // này, còn hơn màn hình làm bài trắng hoặc lỗi 500 hoàn toàn.
                        LOG.warn("Không đọc/ghi được mốc thời gian Redis cho quiz {}:{} — trả về đủ thời gian",
                                        loaiNguon, maBaiKiemTra, ex);
                        return (int) tongGiay;
                }
        }

        /** Xóa mốc thời gian bắt đầu sau khi đã nộp bài — lần làm lại tiếp theo (nếu được phép) sẽ tính lại từ đầu. */
        private void xoaThoiGianBatDauQuiz(Long maNguoiDung, String loaiNguon, Long maBaiKiemTra) {
                redisTemplate.delete("quiz-batdau:" + maNguoiDung + ":" + loaiNguon + ":" + maBaiKiemTra);
        }

        @Override
        @Transactional(readOnly = true)
        public PhanHoiDeKiemTraChoAdmin layDeBaiChoAdmin(String loaiNguon, Long maThamChieu) {
                if ("GOC".equals(loaiNguon)) {
                        return layDeBaiGocChoAdmin(maThamChieu);
                }
                if ("BO_SUNG".equals(loaiNguon)) {
                        return layDeBoSungChoAdmin(maThamChieu);
                }
                throw new NgoaiLeDuLieuKhongHopLe("loaiNguon không hợp lệ: " + loaiNguon);
        }

        private PhanHoiDeKiemTraChoAdmin layDeBaiGocChoAdmin(Long maBaiHoc) {
                PhanHoiBaiHoc baiHoc = quanLyNoiDungDichVu.layChiTietBaiHoc(maBaiHoc)
                                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học", maBaiHoc));
                if (baiHoc.baiKiemTra() == null) {
                        throw new NgoaiLeDuLieuKhongHopLe("Bài học này không phải loại Quiz");
                }
                var bkt = baiHoc.baiKiemTra();
                // layChiTietBaiHoc() dùng nhánh KHÔNG ẩn dapAnDung (dành cho giảng
                // viên/admin soạn thảo — khác layGiaoTrinhHienThi dùng cho học
                // viên) nên câu hỏi ở đây đã có sẵn đáp án đúng, chỉ cần map sang
                // DTO ChoAdmin.
                List<PhanHoiCauHoiChoAdmin> dsCauHoi = bkt.danhSachCauHoi().stream()
                                .map(c -> new PhanHoiCauHoiChoAdmin(
                                                c.maCauHoi(), c.noiDung(), c.viTri(), c.loaiCauHoi(),
                                                c.dapAn().stream()
                                                                .map(d -> new PhanHoiDapAnChoAdmin(d.id(), d.noiDung(),
                                                                                d.dapAnDung()))
                                                                .toList()))
                                .toList();
                return new PhanHoiDeKiemTraChoAdmin("GOC", bkt.maBaiKiemTra(), baiHoc.tieuDe(),
                                bkt.diemToiThieu(), bkt.thoiGian(), dsCauHoi);
        }

        private PhanHoiDeKiemTraChoAdmin layDeBoSungChoAdmin(Long maBaiHocBoSung) {
                BaiHocBoSung b = baiHocBoSungKho.findById(maBaiHocBoSung)
                                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học bổ sung", maBaiHocBoSung));
                if (b.getLoai() != LoaiHocLieuBoSung.QUIZ) {
                        throw new NgoaiLeDuLieuKhongHopLe("Bài học bổ sung này không phải loại Quiz");
                }
                BaiKiemTraBoSung bkt = baiKiemTraBoSungKho.findByBaiBoSung_MaBaiHocBoSung(maBaiHocBoSung)
                                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("đề kiểm tra bổ sung",
                                                maBaiHocBoSung));

                List<PhanHoiCauHoiChoAdmin> dsCauHoi = cauHoiBoSungKho
                                .findByBaiKiemTraBoSung_MaBaiKiemTraBoSungOrderByViTriAsc(bkt.getMaBaiKiemTraBoSung())
                                .stream()
                                .map(c -> new PhanHoiCauHoiChoAdmin(
                                                c.getMaCauHoiBaiBoSung(), c.getNoiDung(), c.getViTri(),
                                                c.getLoaiCauHoi().name(),
                                                c.getDapAn().stream()
                                                                .map(d -> new PhanHoiDapAnChoAdmin(d.id(), d.noiDung(),
                                                                                d.dapAnDung()))
                                                                .toList()))
                                .toList();

                return new PhanHoiDeKiemTraChoAdmin("BO_SUNG", bkt.getMaBaiKiemTraBoSung(), b.getTieuDe(),
                                bkt.getDiemToiThieu(), bkt.getThoiGian(), dsCauHoi);
        }

        private void kiemTraLaThanhVien(Long maNguoiDung, Long maLopHoc) {
                // 1. Kiểm tra xem có phải học viên không (Nếu đúng thì cho qua luôn để tối ưu
                // DB)
                boolean laHocVien = thanhVienLopKho.existsByMaHocVienAndMaLopHoc(maNguoiDung, maLopHoc);
                if (laHocVien) {
                        return;
                }

                // 2. Nếu không phải học viên, kiểm tra xem có phải Giảng viên quản lý lớp này
                // không — CHỈ dùng cho hành động XEM (xem đề bài trước), KHÔNG dùng cho hành
                // động NỘP BÀI (xem kiemTraDuocNopBai bên dưới).
                LopHoc lop = lopHocKho.findById(maLopHoc)
                                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học", maLopHoc));

                boolean laGiangVien = lop.getMaGiangVien().equals(maNguoiDung);

                // 3. Nếu KHÔNG phải Học viên và CŨNG KHÔNG PHẢI Giảng viên thì mới báo lỗi
                if (!laGiangVien) {
                        throw new NgoaiLeKhongCoQuyen("Bạn chưa tham gia lớp học này");
                }
        }

        /**
         * FIX: dùng riêng cho các hành động NỘP BÀI kiểm tra (nopBaiGoc,
         * nopBaiBoSung) — CHỈ cho phép học viên thật (có trong thanh_vien_lop),
         * KHÔNG cho phép giảng viên, dù giảng viên có quyền xem đề bài trước
         * (kiemTraLaThanhVien).
         *
         * Trước đây nopBaiGoc/nopBaiBoSung dùng chung kiemTraLaThanhVien với
         * layDeBaiGoc/layDeBoSung — nghĩa là giảng viên vô tình được phép nộp
         * bài thật, tạo ra KetQuaQuiz + TienDoBaiHoc như thể họ là học viên
         * của chính lớp mình dạy. Hệ quả xa hơn: tiến độ "ảo" đó đủ điều kiện
         * để giảng viên tự cấp Giấy xác nhận lớp học cho chính mình (xem thêm
         * chặn ở GiayXacNhanDichVuImpl.capGiayLopHoc).
         */
        private void kiemTraDuocNopBai(Long maNguoiDung, Long maLopHoc) {
                boolean laHocVien = thanhVienLopKho.existsByMaHocVienAndMaLopHoc(maNguoiDung, maLopHoc);
                if (!laHocVien) {
                        throw new NgoaiLeKhongCoQuyen(
                                        "Chỉ học viên đã tham gia lớp học mới được nộp bài kiểm tra. "
                                                        + "Giảng viên chỉ có quyền xem trước đề bài, không tính là học viên của lớp mình dạy.");
                }
        }

        /**
         * Kiểm tra quyền làm quiz khi TỰ HỌC — chỉ cần đã sở hữu khóa học
         * chứa bài học này (bất kỳ loại giấy phép), không yêu cầu tham gia
         * lớp nào (đối xứng với TienDoHocDichVuImpl.capNhatTienDoCaNhan).
         */
        private void kiemTraQuyenTuHoc(Long maNguoiDung, Long maBaiHoc) {
                BaiHoc baiHoc = baiHocKho.findById(maBaiHoc)
                                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài học", maBaiHoc));
                Long maKhoaHoc = baiHoc.getChuong().getKhoaHoc().getMaKhoaHoc();
                if (!quyenSuDungKho.existsByMaNguoiDungAndMaKhoaHoc(maNguoiDung, maKhoaHoc)) {
                        throw new NgoaiLeKhongCoQuyen("Bạn chưa sở hữu khóa học này");
                }
        }
}
