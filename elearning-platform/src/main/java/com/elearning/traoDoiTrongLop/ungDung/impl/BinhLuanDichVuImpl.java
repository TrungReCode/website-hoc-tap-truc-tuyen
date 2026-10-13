package com.elearning.traoDoiTrongLop.ungDung.impl;

import com.elearning.dinhDanhPhanQuyen.ungDung.HoSoCongKhaiDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.ThongTinNguoiDungCoBan;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiThanhVienLop;
import com.elearning.lopHocLMS.ungDung.GhiDanhDichVu;
import com.elearning.lopHocLMS.ungDung.TaoLopHocDichVu;
import com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi.PhanHoiBinhLuan;
import com.elearning.traoDoiTrongLop.giaoDien.dto.phanHoi.PhanHoiTrangThaiNopBai;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauChamDiem;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauNopBaiTap;
import com.elearning.traoDoiTrongLop.giaoDien.dto.yeuCau.YeuCauTaoBinhLuan;
import com.elearning.traoDoiTrongLop.haTang.BaiDangKho;
import com.elearning.traoDoiTrongLop.haTang.BinhLuanKho;
import com.elearning.traoDoiTrongLop.haTang.ThongBaoKho;
import com.elearning.traoDoiTrongLop.mienDomain.BaiDang;
import com.elearning.traoDoiTrongLop.mienDomain.BinhLuan;
import com.elearning.traoDoiTrongLop.mienDomain.TepDinhKem;
import com.elearning.traoDoiTrongLop.mienDomain.ThongBao;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiBaiDang;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;
import com.elearning.traoDoiTrongLop.ungDung.BinhLuanDichVu;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bài nộp/chấm điểm (kiểu Google Classroom) cho bài đăng loại BAI_TAP được
 * triển khai TRÊN CHÍNH bảng binh_luan đã có, KHÔNG thêm bảng/cột CSDL mới:
 *
 * - Bài nộp = một bình luận GỐC (maCha = null) do học viên tạo, với
 * noiDung bắt đầu bằng tiền tố TIEN_TO_NOP_BAI, theo sau là JSON của
 * NoiDungNopBai (ghi chú + danh sách file — tái dùng TepDinhKem JSONB
 * value-object đã có sẵn cho bài đăng).
 * - Điểm/nhận xét = một REPLY (maCha = mã bài nộp) do giảng viên tạo, với
 * noiDung bắt đầu bằng tiền tố TIEN_TO_CHAM_DIEM, theo sau là JSON của
 * NoiDungChamDiem (điểm/điểm tối đa/nhận xét).
 *
 * Vì taoBinhLuan() (bình luận thường) bị CHẶN hẳn trên bài đăng loại
 * BAI_TAP (xem bên dưới), hai tiền tố này không bao giờ xung đột với nội
 * dung bình luận thật của người dùng — chỉ 2 method nopBaiTap()/chamDiem()
 * mới có thể tạo ra bình luận mang tiền tố này.
 */
@Service
@RequiredArgsConstructor
@Transactional
class BinhLuanDichVuImpl implements BinhLuanDichVu {

    private static final String TIEN_TO_NOP_BAI = "\u00A7NOP_BAI\u00A7";
    private static final String TIEN_TO_CHAM_DIEM = "\u00A7CHAM_DIEM\u00A7";

    private final BinhLuanKho binhLuanKho;
    private final BaiDangKho baiDangKho;
    private final ThongBaoKho thongBaoKho;
    private final GhiDanhDichVu ghiDanhDichVu;
    private final TaoLopHocDichVu taoLopHocDichVu;
    private final HoSoCongKhaiDichVu hoSoCongKhaiDichVu;
    private final ObjectMapper objectMapper;

    @Override
    public PhanHoiBinhLuan taoBinhLuan(Long maNguoiDung, Long maBaiDang, YeuCauTaoBinhLuan yeuCau) {
        BaiDang baiDang = timBaiDangHoacNem(maBaiDang);
        kiemTraTruyCap(maNguoiDung, baiDang.getMaLopHoc());

        if (baiDang.getLoai() == LoaiBaiDang.BAI_TAP) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Bài đăng loại Bài tập không hỗ trợ bình luận thông thường — "
                            + "học viên hãy dùng chức năng nộp bài, giảng viên hãy dùng chức năng chấm điểm");
        }

        // Nếu là reply, kiểm tra bình luận cha tồn tại và là bình luận gốc
        if (yeuCau.maCha() != null) {
            BinhLuan cha = binhLuanKho.findById(yeuCau.maCha())
                    .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bình luận cha", yeuCau.maCha()));
            if (!cha.laBinhLuanGoc()) {
                throw new NgoaiLeDuLieuKhongHopLe("Không thể reply vào một reply — chỉ được reply bình luận gốc");
            }
            if (!cha.getMaBaiDang().equals(maBaiDang)) {
                throw new NgoaiLeDuLieuKhongHopLe("Bình luận cha không thuộc bài đăng này");
            }
        }

        BinhLuan binhLuan = new BinhLuan(maBaiDang, maNguoiDung, yeuCau.maCha(), yeuCau.noiDung());
        binhLuanKho.save(binhLuan);

        // Gửi thông báo cho chủ bài đăng (nếu không phải người bình luận)
        if (!baiDang.getMaNguoiDang().equals(maNguoiDung)) {
            ThongTinNguoiDungCoBan nguoiBinhLuan = hoSoCongKhaiDichVu.layThongTinCoBan(maNguoiDung).orElse(null);
            String ten = nguoiBinhLuan != null ? nguoiBinhLuan.hoTen() : "Một thành viên";

            Map<String, Object> du = new HashMap<>();
            du.put("maBaiDang", maBaiDang);
            du.put("maBinhLuan", binhLuan.getMaBinhLuan());
            du.put("maLopHoc", baiDang.getMaLopHoc());
            du.put("tieuDeBaiDang", baiDang.getTieuDe());

            thongBaoKho.save(new ThongBao(
                    baiDang.getMaNguoiDang(),
                    LoaiThongBao.BINH_LUAN_MOI,
                    "Có bình luận mới trên bài đăng của bạn",
                    ten + " đã bình luận về \"" + baiDang.getTieuDe() + "\"",
                    du));
        }

        return sangPhanHoi(binhLuan, List.of());
    }

    @Override
    public void xoaBinhLuan(Long maNguoiDung, Long maBinhLuan) {
        BinhLuan binhLuan = binhLuanKho.findById(maBinhLuan)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bình luận", maBinhLuan));

        BaiDang baiDang = timBaiDangHoacNem(binhLuan.getMaBaiDang());

        // Chủ sở hữu hoặc giảng viên lớp được xoá
        if (!binhLuan.laChuSoHuu(maNguoiDung)) {
            if (!taoLopHocDichVu.laGiangVienCuaLop(maNguoiDung, baiDang.getMaLopHoc())) {
                throw new NgoaiLeKhongCoQuyen("Bạn không có quyền xoá bình luận này");
            }
        }

        // Nếu là bình luận gốc, xoá tất cả reply trước
        if (binhLuan.laBinhLuanGoc()) {
            List<BinhLuan> replies = binhLuanKho.findByMaChaOrderByNgayTaoAsc(maBinhLuan);
            binhLuanKho.deleteAll(replies);
        }
        binhLuanKho.delete(binhLuan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiBinhLuan> layBinhLuanTheoBAiDang(Long maNguoiDung, Long maBaiDang) {
        BaiDang baiDang = timBaiDangHoacNem(maBaiDang);
        kiemTraTruyCap(maNguoiDung, baiDang.getMaLopHoc());

        List<BinhLuan> binhLuanGoc = binhLuanKho
                .findByMaBaiDangAndMaChaIsNullOrderByNgayTaoAsc(maBaiDang);

        // Bài tập: học viên chỉ xem được bài nộp của CHÍNH MÌNH (kèm điểm/nhận
        // xét nếu có) — không được xem bài nộp của bạn học khác. Giảng viên
        // của lớp thì xem được toàn bộ.
        if (baiDang.getLoai() == LoaiBaiDang.BAI_TAP
                && !taoLopHocDichVu.laGiangVienCuaLop(maNguoiDung, baiDang.getMaLopHoc())) {
            binhLuanGoc = binhLuanGoc.stream()
                    .filter(bl -> bl.getMaNguoiDung().equals(maNguoiDung))
                    .toList();
        }

        // Batch-load tất cả replies và thông tin người dùng
        List<Long> dsMaBinhLuanGoc = binhLuanGoc.stream().map(BinhLuan::getMaBinhLuan).toList();
        Map<Long, List<BinhLuan>> bangReply = dsMaBinhLuanGoc.stream()
                .collect(Collectors.toMap(
                        ma -> ma,
                        ma -> binhLuanKho.findByMaChaOrderByNgayTaoAsc(ma)));

        // Thu thập tất cả mã người dùng để batch-load một lần
        List<Long> tatCaMa = binhLuanGoc.stream().map(BinhLuan::getMaNguoiDung).collect(Collectors.toList());
        bangReply.values().forEach(replies -> replies.forEach(r -> tatCaMa.add(r.getMaNguoiDung())));
        Map<Long, ThongTinNguoiDungCoBan> bangNguoiDung = hoSoCongKhaiDichVu.layThongTinCoBanNhieu(tatCaMa);

        return binhLuanGoc.stream()
                .map(bl -> {
                    List<PhanHoiBinhLuan> danhSachReply = bangReply.getOrDefault(bl.getMaBinhLuan(), List.of())
                            .stream().map(r -> sangPhanHoi(r, List.of(), bangNguoiDung)).toList();
                    return sangPhanHoi(bl, danhSachReply, bangNguoiDung);
                })
                .toList();
    }

    // ──────────────── Nộp bài / Chấm điểm (Bài tập) ────────────────

    @Override
    public PhanHoiBinhLuan nopBaiTap(Long maNguoiDungHocVien, Long maBaiDang, YeuCauNopBaiTap yeuCau) {
        BaiDang baiDang = timBaiDangHoacNem(maBaiDang);

        if (baiDang.getLoai() != LoaiBaiDang.BAI_TAP) {
            throw new NgoaiLeDuLieuKhongHopLe("Chỉ có thể nộp bài cho bài đăng loại Bài tập");
        }
        if (taoLopHocDichVu.laGiangVienCuaLop(maNguoiDungHocVien, baiDang.getMaLopHoc())) {
            throw new NgoaiLeDuLieuKhongHopLe("Giảng viên không nộp bài tập");
        }
        if (!ghiDanhDichVu.laThanhVien(maNguoiDungHocVien, baiDang.getMaLopHoc())) {
            throw new NgoaiLeKhongCoQuyen("Bạn không phải học viên của lớp học này");
        }
        if ((yeuCau.ghiChu() == null || yeuCau.ghiChu().isBlank())
                && (yeuCau.tepDinhKem() == null || yeuCau.tepDinhKem().isEmpty())) {
            throw new NgoaiLeDuLieuKhongHopLe("Vui lòng nhập ghi chú hoặc đính kèm ít nhất 1 tệp bài làm");
        }

        String noiDungMaHoa = maHoaNopBai(yeuCau.ghiChu(), yeuCau.tepDinhKem());

        BinhLuan baiNop = binhLuanKho.findByMaBaiDangAndMaNguoiDungAndMaChaIsNull(maBaiDang, maNguoiDungHocVien)
                .filter(bl -> loaiCuaBinhLuan(bl).equals("NOP_BAI"))
                .orElse(null);

        boolean laNopLai = baiNop != null;
        if (baiNop == null) {
            baiNop = new BinhLuan(maBaiDang, maNguoiDungHocVien, null, noiDungMaHoa);
        } else {
            baiNop.setNoiDung(noiDungMaHoa);
        }
        binhLuanKho.save(baiNop);

        // Thông báo cho giảng viên (chủ bài đăng) có bài nộp mới/nộp lại
        ThongTinNguoiDungCoBan hocVien = hoSoCongKhaiDichVu.layThongTinCoBan(maNguoiDungHocVien).orElse(null);
        String tenHocVien = hocVien != null ? hocVien.hoTen() : "Một học viên";
        Map<String, Object> du = new HashMap<>();
        du.put("maBaiDang", maBaiDang);
        du.put("maBinhLuan", baiNop.getMaBinhLuan());
        du.put("maLopHoc", baiDang.getMaLopHoc());
        du.put("tieuDeBaiDang", baiDang.getTieuDe());
        thongBaoKho.save(new ThongBao(
                baiDang.getMaNguoiDang(),
                LoaiThongBao.BINH_LUAN_MOI,
                laNopLai ? "Học viên đã nộp lại bài tập" : "Có bài nộp mới",
                tenHocVien + " đã nộp bài cho \"" + baiDang.getTieuDe() + "\"",
                du));

        return sangPhanHoi(baiNop, layReplyDaChuyenDoi(baiNop.getMaBinhLuan()));
    }

    @Override
    public PhanHoiBinhLuan chamDiem(Long maNguoiDungGiangVien, Long maBinhLuanBaiNop, YeuCauChamDiem yeuCau) {
        BinhLuan baiNop = binhLuanKho.findById(maBinhLuanBaiNop)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài nộp", maBinhLuanBaiNop));

        if (!baiNop.laBinhLuanGoc() || !loaiCuaBinhLuan(baiNop).equals("NOP_BAI")) {
            throw new NgoaiLeDuLieuKhongHopLe("Bình luận này không phải một bài nộp");
        }

        BaiDang baiDang = timBaiDangHoacNem(baiNop.getMaBaiDang());
        if (!taoLopHocDichVu.laGiangVienCuaLop(maNguoiDungGiangVien, baiDang.getMaLopHoc())) {
            throw new NgoaiLeKhongCoQuyen("Chỉ giảng viên của lớp mới có quyền chấm điểm");
        }
        if (yeuCau.diem().compareTo(yeuCau.diemToiDa()) > 0) {
            throw new NgoaiLeDuLieuKhongHopLe("Điểm không được lớn hơn điểm tối đa");
        }
        if (yeuCau.diem().compareTo(BigDecimal.ZERO) < 0) {
            throw new NgoaiLeDuLieuKhongHopLe("Điểm không được nhỏ hơn 0");
        }

        String noiDungMaHoa = maHoaChamDiem(yeuCau.diem(), yeuCau.diemToiDa(), yeuCau.nhanXet());

        List<BinhLuan> replyHienCo = binhLuanKho.findByMaChaOrderByNgayTaoAsc(maBinhLuanBaiNop);
        BinhLuan phanHoiChamDiem = replyHienCo.stream()
                .filter(r -> loaiCuaBinhLuan(r).equals("CHAM_DIEM"))
                .findFirst()
                .orElse(null);

        boolean laChamLai = phanHoiChamDiem != null;
        if (phanHoiChamDiem == null) {
            phanHoiChamDiem = new BinhLuan(baiNop.getMaBaiDang(), maNguoiDungGiangVien, maBinhLuanBaiNop, noiDungMaHoa);
        } else {
            phanHoiChamDiem.setNoiDung(noiDungMaHoa);
        }
        binhLuanKho.save(phanHoiChamDiem);

        // Thông báo điểm cho học viên
        Map<String, Object> du = new HashMap<>();
        du.put("maBaiDang", baiDang.getMaBaiDang());
        du.put("maBinhLuan", baiNop.getMaBinhLuan());
        du.put("maLopHoc", baiDang.getMaLopHoc());
        du.put("tieuDeBaiDang", baiDang.getTieuDe());
        thongBaoKho.save(new ThongBao(
                baiNop.getMaNguoiDung(),
                LoaiThongBao.BINH_LUAN_MOI,
                laChamLai ? "Bài nộp của bạn đã được chấm lại" : "Bài nộp của bạn đã được chấm điểm",
                "Điểm: " + yeuCau.diem() + "/" + yeuCau.diemToiDa()
                        + " cho \"" + baiDang.getTieuDe() + "\"",
                du));

        return sangPhanHoi(baiNop, layReplyDaChuyenDoi(baiNop.getMaBinhLuan()));
    }

    /**
     * Đối chiếu danh sách thành viên lớp (module lopHocLMS) với các bình
     * luận NOP_BAI/CHAM_DIEM hiện có của bài đăng (module traoDoiTrongLop)
     * để trả về trạng thái nộp bài của TỪNG học viên — không cần thêm
     * bảng/cột CSDL, chỉ tổng hợp dữ liệu đã có sẵn ở hai module.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiTrangThaiNopBai> layDanhSachNopBai(Long maNguoiDungGiangVien, Long maBaiDang) {
        BaiDang baiDang = timBaiDangHoacNem(maBaiDang);

        if (baiDang.getLoai() != LoaiBaiDang.BAI_TAP) {
            throw new NgoaiLeDuLieuKhongHopLe("Chỉ bài đăng loại Bài tập mới có danh sách nộp bài");
        }
        if (!taoLopHocDichVu.laGiangVienCuaLop(maNguoiDungGiangVien, baiDang.getMaLopHoc())) {
            throw new NgoaiLeKhongCoQuyen("Chỉ giảng viên của lớp mới xem được danh sách nộp bài");
        }

        // Toàn bộ thành viên (học viên) của lớp — đã có sẵn ở module lopHocLMS
        List<PhanHoiThanhVienLop> thanhVien = ghiDanhDichVu.layThanhVienCuaLop(baiDang.getMaLopHoc());

        // Toàn bộ bài nộp (bình luận gốc loại NOP_BAI) hiện có của bài đăng này
        Map<Long, BinhLuan> baiNopTheoHocVien = binhLuanKho
                .findByMaBaiDangAndMaChaIsNullOrderByNgayTaoAsc(maBaiDang).stream()
                .filter(bl -> loaiCuaBinhLuan(bl).equals("NOP_BAI"))
                .collect(Collectors.toMap(BinhLuan::getMaNguoiDung, bl -> bl));

        List<PhanHoiTrangThaiNopBai> ketQua = thanhVien.stream()
                .map(hv -> {
                    BinhLuan baiNop = baiNopTheoHocVien.get(hv.maHocVien());
                    if (baiNop == null) {
                        return new PhanHoiTrangThaiNopBai(
                                hv.maHocVien(), hv.tenHocVien(), hv.anhDaiDienHocVien(),
                                "CHUA_NOP", null, null, null, null);
                    }

                    BinhLuan phanHoiChamDiem = binhLuanKho.findByMaChaOrderByNgayTaoAsc(baiNop.getMaBinhLuan())
                            .stream()
                            .filter(r -> loaiCuaBinhLuan(r).equals("CHAM_DIEM"))
                            .findFirst().orElse(null);

                    if (phanHoiChamDiem == null) {
                        return new PhanHoiTrangThaiNopBai(
                                hv.maHocVien(), hv.tenHocVien(), hv.anhDaiDienHocVien(),
                                "DA_NOP", baiNop.getNgayTao(), baiNop.getMaBinhLuan(), null, null);
                    }

                    NoiDungChamDiem gm = giaiMaChamDiem(phanHoiChamDiem);
                    return new PhanHoiTrangThaiNopBai(
                            hv.maHocVien(), hv.tenHocVien(), hv.anhDaiDienHocVien(),
                            "DA_CHAM", baiNop.getNgayTao(), baiNop.getMaBinhLuan(), gm.diem(), gm.diemToiDa());
                })
                .collect(Collectors.toList());

        // Sắp xếp để "Chưa nộp" hiện lên trước — giúp giảng viên nhắc nhở nhanh
        Map<String, Integer> thuTuTrangThai = Map.of("CHUA_NOP", 0, "DA_NOP", 1, "DA_CHAM", 2);
        ketQua.sort(Comparator.comparing(k -> thuTuTrangThai.getOrDefault(k.trangThai(), 9)));
        return ketQua;
    }

    private record NoiDungNopBai(String ghiChu, List<TepDinhKem> tepDinhKem) {
    }

    private record NoiDungChamDiem(BigDecimal diem, BigDecimal diemToiDa, String nhanXet) {
    }

    private String maHoaNopBai(String ghiChu, List<TepDinhKem> tepDinhKem) {
        try {
            return TIEN_TO_NOP_BAI + objectMapper.writeValueAsString(
                    new NoiDungNopBai(ghiChu, tepDinhKem != null ? tepDinhKem : List.of()));
        } catch (Exception e) {
            throw new NgoaiLeDuLieuKhongHopLe("Không thể xử lý dữ liệu bài nộp");
        }
    }

    private String maHoaChamDiem(BigDecimal diem, BigDecimal diemToiDa, String nhanXet) {
        try {
            return TIEN_TO_CHAM_DIEM + objectMapper.writeValueAsString(
                    new NoiDungChamDiem(diem, diemToiDa, nhanXet));
        } catch (Exception e) {
            throw new NgoaiLeDuLieuKhongHopLe("Không thể xử lý dữ liệu chấm điểm");
        }
    }

    private String loaiCuaBinhLuan(BinhLuan bl) {
        String nd = bl.getNoiDung();
        if (nd == null)
            return "BINH_LUAN";
        if (nd.startsWith(TIEN_TO_NOP_BAI))
            return "NOP_BAI";
        if (nd.startsWith(TIEN_TO_CHAM_DIEM))
            return "CHAM_DIEM";
        return "BINH_LUAN";
    }

    private NoiDungNopBai giaiMaNopBai(BinhLuan bl) {
        try {
            return objectMapper.readValue(
                    bl.getNoiDung().substring(TIEN_TO_NOP_BAI.length()), NoiDungNopBai.class);
        } catch (Exception e) {
            return new NoiDungNopBai(bl.getNoiDung(), List.of());
        }
    }

    private NoiDungChamDiem giaiMaChamDiem(BinhLuan bl) {
        try {
            return objectMapper.readValue(
                    bl.getNoiDung().substring(TIEN_TO_CHAM_DIEM.length()), NoiDungChamDiem.class);
        } catch (Exception e) {
            return new NoiDungChamDiem(null, null, bl.getNoiDung());
        }
    }

    /**
     * Dùng cho nopBaiTap/chamDiem — tải lại danh sách reply đã chuyển đổi định dạng
     * cho một bài nộp.
     */
    private List<PhanHoiBinhLuan> layReplyDaChuyenDoi(Long maBinhLuanGoc) {
        return binhLuanKho.findByMaChaOrderByNgayTaoAsc(maBinhLuanGoc).stream()
                .map(r -> sangPhanHoi(r, List.of()))
                .toList();
    }

    // ──────────────── Helper ────────────────

    private BaiDang timBaiDangHoacNem(Long maBaiDang) {
        BaiDang b = baiDangKho.findById(maBaiDang)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("bài đăng", maBaiDang));
        if (Boolean.TRUE.equals(b.getXoaMem())) {
            throw NgoaiLeKhongTimThay.cuaTaiNguyen("bài đăng", maBaiDang);
        }
        return b;
    }

    private void kiemTraTruyCap(Long maNguoiDung, Long maLopHoc) {
        boolean laGiangVien = taoLopHocDichVu.laGiangVienCuaLop(maNguoiDung, maLopHoc);
        boolean laThanhVien = ghiDanhDichVu.laThanhVien(maNguoiDung, maLopHoc);
        if (!laGiangVien && !laThanhVien) {
            throw new NgoaiLeKhongCoQuyen("Bạn không phải thành viên của lớp học này");
        }
    }

    private PhanHoiBinhLuan sangPhanHoi(BinhLuan bl, List<PhanHoiBinhLuan> danhSachReply) {
        ThongTinNguoiDungCoBan nd = hoSoCongKhaiDichVu.layThongTinCoBan(bl.getMaNguoiDung()).orElse(null);
        return xayDungPhanHoi(bl, danhSachReply, nd != null ? nd.hoTen() : null, nd != null ? nd.anhDaiDien() : null);
    }

    private PhanHoiBinhLuan sangPhanHoi(BinhLuan bl, List<PhanHoiBinhLuan> danhSachReply,
            Map<Long, ThongTinNguoiDungCoBan> bang) {
        ThongTinNguoiDungCoBan nd = bang.get(bl.getMaNguoiDung());
        return xayDungPhanHoi(bl, danhSachReply, nd != null ? nd.hoTen() : null, nd != null ? nd.anhDaiDien() : null);
    }

    private PhanHoiBinhLuan xayDungPhanHoi(BinhLuan bl, List<PhanHoiBinhLuan> danhSachReply,
            String tenNguoiDung, String anhDaiDien) {
        String loai = loaiCuaBinhLuan(bl);
        String noiDungHienThi = bl.getNoiDung();
        List<TepDinhKem> tepDinhKemNopBai = null;
        BigDecimal diem = null;
        BigDecimal diemToiDa = null;

        if (loai.equals("NOP_BAI")) {
            NoiDungNopBai gm = giaiMaNopBai(bl);
            noiDungHienThi = gm.ghiChu();
            tepDinhKemNopBai = gm.tepDinhKem();
        } else if (loai.equals("CHAM_DIEM")) {
            NoiDungChamDiem gm = giaiMaChamDiem(bl);
            noiDungHienThi = gm.nhanXet();
            diem = gm.diem();
            diemToiDa = gm.diemToiDa();
        }

        return new PhanHoiBinhLuan(bl.getMaBinhLuan(), bl.getMaBaiDang(), bl.getMaNguoiDung(),
                tenNguoiDung, anhDaiDien, bl.getMaCha(), noiDungHienThi, bl.getNgayTao(), danhSachReply,
                loai, tepDinhKemNopBai, diem, diemToiDa);
    }
}
