package com.elearning.lopHocLMS.ungDung.impl;

import com.elearning.choKhoaHoc.ungDung.ThongTinKhoaHocCoBan;
import com.elearning.choKhoaHoc.ungDung.TimKiemKhoaHocDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.HoSoCongKhaiDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.ThongTinNguoiDungCoBan;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.dungChung.ngoaiLe.NgoaiLeNghiepVu;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLoiMoi;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLop;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiThanhVienLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauMoiHocVien;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauThamGiaBangMa;
import com.elearning.lopHocLMS.haTang.LoiMoiKho;
import com.elearning.lopHocLMS.haTang.LopHocKho;
import com.elearning.thanhToan.haTang.PhieuTraPhiLopHocKho;
import com.elearning.lopHocLMS.haTang.QuyenSuDungKho;
import com.elearning.lopHocLMS.haTang.ThanhVienLopKho;
import com.elearning.lopHocLMS.mienDomain.LoiMoi;
import com.elearning.lopHocLMS.mienDomain.LopHoc;
import com.elearning.lopHocLMS.mienDomain.QuyenSuDung;
import com.elearning.lopHocLMS.mienDomain.ThanhVienLop;
import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiGiayPhep;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLoiMoi;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;
import com.elearning.lopHocLMS.ungDung.GhiDanhDichVu;
import com.elearning.lopHocLMS.ungDung.TienDoHocDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * UC-06 Tham gia lớp học — cơ chế Khóa kép.
 *
 * "Khóa thứ nhất" (sở hữu khóa học gốc) được lopHocLMS tự quản lý đầy đủ
 * qua bảng quyen_su_dung: nếu khóa học gốc MIỄN PHÍ (gia = 0) hoặc người
 * tham gia chính là tác giả gốc, hệ thống tự động cấp Giấy phép Cá nhân
 * (CA_NHAN) ngay khi tham gia lớp đầu tiên; nếu khóa học gốc CÓ phí và
 * chưa sở hữu, chặn lại với NgoaiLeNghiepVu 402 "CHUA_SO_HUU_KHOA_HOC_GOC".
 * Logic này được lộ ra ngoài qua kiemTraKhoaThuNhat(Long, Long) để module
 * thanhToan gọi trước khi tạo đơn thanh toán phí lớp học / cấp quyền truy
 * cập lớp qua IPN — bắt buộc phải gọi ở CẢ HAI nơi đó (xem Javadoc của
 * GhiDanhDichVu.kiemTraKhoaThuNhat), nếu không Khóa 1 sẽ bị bỏ qua hoàn
 * toàn với luồng đóng phí lớp học vì luồng đó không đi qua thucHienGhiDanh().
 *
 * "Khóa thứ hai" (phí lớp học) hoạt động tương tự: lop_hoc.phiLopHoc = 0
 * thì bỏ qua, > 0 thì yêu cầu đã có PhieuTraPhiLopHoc trạng thái THANH_CONG
 * (xem kiemTraKhoaThuHai) — do module thanhToan quản lý.
 */
@Service
@RequiredArgsConstructor
@Transactional
class GhiDanhDichVuImpl implements GhiDanhDichVu {

    private static final int SO_NGAY_HET_HAN_LOI_MOI = 7;

    private final LopHocKho lopHocKho;
    private final LoiMoiKho loiMoiKho;
    private final ThanhVienLopKho thanhVienLopKho;
    private final QuyenSuDungKho quyenSuDungKho;
    private final PhieuTraPhiLopHocKho phieuTraPhiLopHocKho;
    private final TimKiemKhoaHocDichVu timKiemKhoaHocDichVu;
    private final HoSoCongKhaiDichVu hoSoCongKhaiDichVu;
    private final TienDoHocDichVu tienDoHocDichVu;
    private final LopMapper lopMapper;

    @Override
    public List<PhanHoiLoiMoi> moiHocVien(Long maNguoiDungGiangVien, Long maLopHoc, YeuCauMoiHocVien yeuCau) {
        LopHoc lop = timHoacNem(maLopHoc);
        kiemTraQuyenGiangVien(lop, maNguoiDungGiangVien);

        LocalDateTime hetHan = LocalDateTime.now().plusDays(SO_NGAY_HET_HAN_LOI_MOI);
        return yeuCau.dsEmail().stream()
                .map(email -> {
                    LoiMoi moi = new LoiMoi(lop, email.trim().toLowerCase(), maNguoiDungGiangVien, hetHan);
                    loiMoiKho.save(moi);
                    return sangPhanHoiLoiMoi(moi);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiLoiMoi> layLoiMoiCuaLop(Long maNguoiDungGiangVien, Long maLopHoc) {
        LopHoc lop = timHoacNem(maLopHoc);
        kiemTraQuyenGiangVien(lop, maNguoiDungGiangVien);
        return loiMoiKho.findByLopHoc_MaLopHocOrderByNgayTaoDesc(maLopHoc).stream().map(this::sangPhanHoiLoiMoi).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiLoiMoi> layLoiMoiCuaToi(Long maNguoiDung, String email) {
        return loiMoiKho.findByEmailHocVienAndTrangThai(email.trim().toLowerCase(), TrangThaiLoiMoi.CHO_XAC_NHAN)
                .stream().map(this::sangPhanHoiLoiMoi).toList();
    }

    @Override
    public PhanHoiLop chapNhanLoiMoi(Long maNguoiDung, String email, Long maLoiMoi) {
        LoiMoi loiMoi = loiMoiKho.findById(maLoiMoi)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lời mời", maLoiMoi));

        if (!loiMoi.getEmailHocVien().equalsIgnoreCase(email)) {
            throw new NgoaiLeKhongCoQuyen("Lời mời này không gửi cho bạn");
        }
        if (loiMoi.getTrangThai() != TrangThaiLoiMoi.CHO_XAC_NHAN) {
            throw new NgoaiLeDuLieuKhongHopLe("Lời mời đã được xử lý trước đó");
        }
        if (!loiMoi.conHan()) {
            throw new NgoaiLeDuLieuKhongHopLe("Lời mời đã hết hạn, vui lòng liên hệ giảng viên để được mời lại");
        }

        LopHoc lop = loiMoi.getLopHoc();
        thucHienGhiDanh(maNguoiDung, lop);
        loiMoi.setTrangThai(TrangThaiLoiMoi.DONG_Y);

        return lopMapper.sangPhanHoiLop(lop);
    }

    @Override
    public void tuChoiLoiMoi(Long maNguoiDung, String email, Long maLoiMoi) {
        LoiMoi loiMoi = loiMoiKho.findById(maLoiMoi)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lời mời", maLoiMoi));
        if (!loiMoi.getEmailHocVien().equalsIgnoreCase(email)) {
            throw new NgoaiLeKhongCoQuyen("Lời mời này không gửi cho bạn");
        }
        if (loiMoi.getTrangThai() != TrangThaiLoiMoi.CHO_XAC_NHAN) {
            throw new NgoaiLeDuLieuKhongHopLe("Lời mời đã được xử lý trước đó");
        }
        loiMoi.setTrangThai(TrangThaiLoiMoi.TU_CHOI);
    }

    @Override
    public PhanHoiLop thamGiaBangMa(Long maNguoiDung, YeuCauThamGiaBangMa yeuCau) {
        LopHoc lop = lopHocKho.findByMaLop(yeuCau.maLop().trim().toUpperCase())
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học với mã", yeuCau.maLop()));

        if (!Boolean.TRUE.equals(lop.getCongKhai())) {
            throw new NgoaiLeKhongCoQuyen("Lớp học này không công khai — bạn cần được giảng viên mời để tham gia");
        }
        thucHienGhiDanh(maNguoiDung, lop);
        return lopMapper.sangPhanHoiLop(lop);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiThanhVienLop> layThanhVien(Long maNguoiDungGiangVien, Long maLopHoc) {
        LopHoc lop = timHoacNem(maLopHoc);
        kiemTraQuyenGiangVien(lop, maNguoiDungGiangVien);

        List<ThanhVienLop> thanhVienList = thanhVienLopKho.findByMaLopHoc(maLopHoc);
        List<Long> dsMaHocVien = thanhVienList.stream().map(ThanhVienLop::getMaHocVien).toList();
        Map<Long, String> bangEmail = hoSoCongKhaiDichVu.layEmailNhieu(dsMaHocVien);

        return thanhVienList.stream()
                .map(tv -> {
                    ThongTinNguoiDungCoBan hocVien = hoSoCongKhaiDichVu.layThongTinCoBan(tv.getMaHocVien()).orElse(null);
                    int tienDo = tienDoHocDichVu.tinhTienDoPhanTram(tv.getMaHocVien(), maLopHoc);
                    return new PhanHoiThanhVienLop(
                            tv.getMaHocVien(),
                            hocVien != null ? hocVien.hoTen() : null,
                            hocVien != null ? hocVien.anhDaiDien() : null,
                            bangEmail.get(tv.getMaHocVien()),
                            tv.getNgayThamGia(),
                            tienDo
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiLop> layLopCuaHocVien(Long maNguoiDung) {
        return thanhVienLopKho.findByMaHocVien(maNguoiDung).stream()
                .map(tv -> lopHocKho.findById(tv.getMaLopHoc()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(lop -> {
                    int tienDo = tienDoHocDichVu.tinhTienDoPhanTram(maNguoiDung, lop.getMaLopHoc());
                    return lopMapper.sangPhanHoiLop(lop, tienDo);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean laThanhVien(Long maNguoiDung, Long maLopHoc) {
        return thanhVienLopKho.existsByMaHocVienAndMaLopHoc(maNguoiDung, maLopHoc);
    }

    // ---------- Hỗ trợ nội bộ ----------

    private void thucHienGhiDanh(Long maNguoiDung, LopHoc lop) {
        Long maLopHoc = lop.getMaLopHoc();
        LopHoc lopDuocTai = lopHocKho.timKhoaVaKhoaTheoMaLopHoc(maLopHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học", maLopHoc));

        if (thanhVienLopKho.existsByMaHocVienAndMaLopHoc(maNguoiDung, lopDuocTai.getMaLopHoc())) {
            return;
        }
        if (lopDuocTai.getTrangThai() == TrangThaiLop.DA_DONG || (lopDuocTai.getNgayKetThuc() != null && lopDuocTai.getNgayKetThuc().isBefore(LocalDateTime.now()))) {
            throw new NgoaiLeDuLieuKhongHopLe("Lớp học đã đóng, không thể tham gia");
        }
        if (lopDuocTai.getSiSo() != null && thanhVienLopKho.countByMaLopHoc(lopDuocTai.getMaLopHoc()) >= lopDuocTai.getSiSo()) {
            throw new NgoaiLeDuLieuKhongHopLe("Lớp học đã đủ sĩ số");
        }

        kiemTraVaCapKhoaThuNhat(maNguoiDung, lopDuocTai);
        kiemTraKhoaThuHai(maNguoiDung, lopDuocTai);

        thanhVienLopKho.save(new ThanhVienLop(maNguoiDung, lopDuocTai.getMaLopHoc()));
    }

    @Override
    public void kiemTraKhoaThuNhat(Long maNguoiDung, Long maLopHoc) {
        kiemTraVaCapKhoaThuNhat(maNguoiDung, timHoacNem(maLopHoc));
    }

    /** Khóa 1: sở hữu khóa học gốc. Tự cấp CA_NHAN nếu khóa học miễn phí (xem Javadoc đầu file). */
    private void kiemTraVaCapKhoaThuNhat(Long maNguoiDung, LopHoc lop) {
        if (lop.getMaKhoaHoc() == null) {
            return; // lớp không gắn khóa học gốc (trường hợp hiếm, không cần khóa 1)
        }

        // Nếu lớp được mở với đặc quyền miễn mua khóa gốc (chỉ tác giả gốc mới
        // được bật, xem TaoLopHocDichVuImpl.kiemTraDuDieuKienMoLop) thì bỏ qua
        // Khóa 1 hoàn toàn — không cần kiểm tra sở hữu hay tác giả gì thêm.
        if (Boolean.FALSE.equals(lop.getMuaKhoaHocGoc())) {
            return;
        }

        boolean daSoHuu = quyenSuDungKho.findByMaNguoiDungAndMaKhoaHoc(maNguoiDung, lop.getMaKhoaHoc()).isPresent();
        if (daSoHuu) {
            return;
        }

        // Trường hợp đặc biệt: người tham gia lớp CHÍNH LÀ tác giả gốc của khóa
        // học, nhưng đang vào một lớp do giảng viên khác mở (giảng viên đó sở
        // hữu Giấy phép Giảng dạy — xem kiemTraDuDieuKienMoLop). Tác giả gốc là
        // chủ sở hữu nội dung, không có lý do gì bắt họ mua lại chính sản phẩm
        // của mình trên Chợ khóa học, nên tự động cấp quyền sở hữu CA_NHAN
        // (đủ để vượt Khóa 1 và xem nội dung) thay vì yêu cầu thanh toán.
        if (timKiemKhoaHocDichVu.laTacGiaKhoaHoc(maNguoiDung, lop.getMaKhoaHoc())) {
            quyenSuDungKho.save(new QuyenSuDung(maNguoiDung, lop.getMaKhoaHoc(), LoaiGiayPhep.CA_NHAN));
            return;
        }

        Optional<ThongTinKhoaHocCoBan> khoaHoc = timKiemKhoaHocDichVu.layThongTinCoBan(lop.getMaKhoaHoc());
        boolean mienPhi = khoaHoc.isPresent() && khoaHoc.get().gia() != null
                && khoaHoc.get().gia().compareTo(BigDecimal.ZERO) == 0;

        if (mienPhi) {
            quyenSuDungKho.save(new QuyenSuDung(maNguoiDung, lop.getMaKhoaHoc(), LoaiGiayPhep.CA_NHAN));
            return;
        }

        throw new NgoaiLeNghiepVu(
                "Bạn cần sở hữu khóa học gốc trước khi tham gia lớp học này. "
                        + "Vui lòng mua khóa học tại Chợ khóa học trước.",
                HttpStatus.PAYMENT_REQUIRED, "CHUA_SO_HUU_KHOA_HOC_GOC",
                // Đính kèm maKhoaHoc để FE điều hướng thẳng tới trang chi tiết khóa
                // học cần mua (/khoa-hoc/{maKhoaHoc}) thay vì phải đưa người dùng về
                // Chợ khóa học chung chung rồi tự tìm lại đúng khóa học.
                Map.of("maKhoaHoc", lop.getMaKhoaHoc()));
    }

    /** Khóa 2: phí lớp học (nếu có) — xem Javadoc đầu file về giới hạn hiện tại. */
    private void kiemTraKhoaThuHai(Long maNguoiDung, LopHoc lop) {
        if (!lop.coMienPhi() && !phieuTraPhiLopHocKho.existsByMaNguoiDungAndMaLopHocAndTrangThai(
                maNguoiDung, lop.getMaLopHoc(), com.elearning.thanhToan.mienDomain.TrangThaiGiaoDich.THANH_CONG)) {
            throw new NgoaiLeNghiepVu(
                    "Lớp học này có phí tham gia. Vui lòng thanh toán phí lớp học trước khi tham gia.",
                    HttpStatus.PAYMENT_REQUIRED, "CAN_THANH_TOAN_PHI_LOP");
        }
    }

    private PhanHoiLoiMoi sangPhanHoiLoiMoi(LoiMoi loiMoi) {
        LopHoc lop = loiMoi.getLopHoc();
        ThongTinKhoaHocCoBan kh = lop.getMaKhoaHoc() != null
                ? timKiemKhoaHocDichVu.layThongTinCoBan(lop.getMaKhoaHoc()).orElse(null) : null;
        ThongTinNguoiDungCoBan gv = hoSoCongKhaiDichVu.layThongTinCoBan(lop.getMaGiangVien()).orElse(null);

        return new PhanHoiLoiMoi(
                loiMoi.getMaLoiMoi(), lop.getMaLopHoc(), lop.getMaLop(),
                kh != null ? kh.tieuDe() : null, gv != null ? gv.hoTen() : null,
                loiMoi.getEmailHocVien(), loiMoi.getTrangThai(), loiMoi.getNgayTao(), loiMoi.getNgayHetHan());
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

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiThanhVienLop> layThanhVienCuaLop(Long maLopHoc) {
        return thanhVienLopKho.findByMaLopHoc(maLopHoc).stream()
                .map(tv -> {
                    ThongTinNguoiDungCoBan hocVien =
                            hoSoCongKhaiDichVu.layThongTinCoBan(tv.getMaHocVien()).orElse(null);
                    return new PhanHoiThanhVienLop(
                            tv.getMaHocVien(),
                            hocVien != null ? hocVien.hoTen() : null,
                            hocVien != null ? hocVien.anhDaiDien() : null,
                            null,
                            tv.getNgayThamGia(),
                            null
                    );
                })
                .toList();
    }
}