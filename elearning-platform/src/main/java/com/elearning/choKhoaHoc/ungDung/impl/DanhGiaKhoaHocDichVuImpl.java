package com.elearning.choKhoaHoc.ungDung.impl;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhGia;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauDanhGia;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauXuLyDanhGia;
import com.elearning.choKhoaHoc.haTang.DanhGiaKhoaHocKho;
import com.elearning.choKhoaHoc.haTang.KhoaHocKho;
import com.elearning.choKhoaHoc.mienDomain.DanhGiaKhoaHoc;
import com.elearning.choKhoaHoc.mienDomain.KhoaHoc;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.TrangThaiKhoaHoc;
import com.elearning.choKhoaHoc.ungDung.DanhGiaKhoaHocDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.HoSoCongKhaiDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.ThongTinNguoiDungCoBan;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.dungChung.ngoaiLe.NgoaiLeNghiepVu;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLop;
import com.elearning.lopHocLMS.haTang.QuyenSuDungKho;
import com.elearning.lopHocLMS.ungDung.GhiDanhDichVu;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;
import com.elearning.traoDoiTrongLop.ungDung.ThongBaoDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * UC-15 Đánh giá khóa học.
 *
 * Điều kiện tiền đề: người dùng phải đã sở hữu khóa học (bảng quyen_su_dung,
 * do lopHocLMS quản lý). Nếu sở hữu thông qua việc tham gia một lớp học dựa
 * trên khóa học này, còn yêu cầu thêm tiến độ học tối thiểu trong lớp đó.
 * Trường hợp chỉ sở hữu qua hình thức tự học (CA_NHAN) thì bỏ qua điều kiện
 * tiến độ vì module choKhoaHoc hiện chưa có cơ chế theo dõi tiến độ tự học.
 */
@Service
@RequiredArgsConstructor
@Transactional
class DanhGiaKhoaHocDichVuImpl implements DanhGiaKhoaHocDichVu {

    private static final String TIEN_TO_KHOA = "danh-gia-khoa-hoc:lock:";
    private static final Duration THOI_GIAN_GIU_KHOA = Duration.ofSeconds(5);
    private static final int TIEN_DO_TOI_THIEU_PHAN_TRAM = 20;

    private final DanhGiaKhoaHocKho danhGiaKhoaHocKho;
    private final KhoaHocKho khoaHocKho;
    private final HoSoCongKhaiDichVu hoSoCongKhaiDichVu;
    private final RedisTemplate<String, Object> redisTemplate;
    private final QuyenSuDungKho quyenSuDungKho;
    private final GhiDanhDichVu ghiDanhDichVu;
    private final ThongBaoDichVu thongBaoDichVu;

    @Override
    public PhanHoiDanhGia taoDanhGia(Long maNguoiDung, Long maKhoaHoc, YeuCauDanhGia yeuCau) {
        KhoaHoc khoaHoc = khoaHocKho.findByMaKhoaHocAndTrangThai(maKhoaHoc, TrangThaiKhoaHoc.CONG_KHAI)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("khóa học công khai", maKhoaHoc));

        kiemTraDuDieuKienDanhGia(maNguoiDung, maKhoaHoc);

        String khoa = TIEN_TO_KHOA + maNguoiDung + ":" + maKhoaHoc;
        Boolean chiemDuocKhoa = redisTemplate.opsForValue().setIfAbsent(khoa, "1", THOI_GIAN_GIU_KHOA);
        if (Boolean.FALSE.equals(chiemDuocKhoa)) {
            throw new NgoaiLeNghiepVu("Yêu cầu đánh giá đang được xử lý, vui lòng đợi giây lát rồi thử lại",
                    HttpStatus.TOO_MANY_REQUESTS, "DANG_XU_LY_DANH_GIA");
        }

        try {
            if (danhGiaKhoaHocKho.findByMaNguoiDungAndKhoaHoc_MaKhoaHoc(maNguoiDung, maKhoaHoc).isPresent()) {
                throw new NgoaiLeDuLieuKhongHopLe(
                        "Bạn đã đánh giá khóa học này rồi, vui lòng chỉnh sửa đánh giá hiện có thay vì tạo mới");
            }
            DanhGiaKhoaHoc danhGia = new DanhGiaKhoaHoc(maNguoiDung, khoaHoc, yeuCau.soSao(), yeuCau.nhanXet());
            danhGiaKhoaHocKho.save(danhGia);
            return sangPhanHoi(danhGia);
        } finally {
            redisTemplate.delete(khoa);
        }
    }

    @Override
    public PhanHoiDanhGia capNhatDanhGia(Long maNguoiDung, Long maDanhGia, YeuCauDanhGia yeuCau) {
        DanhGiaKhoaHoc danhGia = timHoacNem(maDanhGia);
        kiemTraQuyenSoHuu(danhGia, maNguoiDung);

        danhGia.setSoSao(yeuCau.soSao());
        danhGia.setNhanXet(yeuCau.nhanXet());
        return sangPhanHoi(danhGia);
    }

    @Override
    public void xoaDanhGia(Long maNguoiDung, Long maDanhGia) {
        DanhGiaKhoaHoc danhGia = timHoacNem(maDanhGia);
        kiemTraQuyenSoHuu(danhGia, maNguoiDung);
        danhGiaKhoaHocKho.delete(danhGia);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiDanhGia> layDanhGiaTheoKhoaHoc(Long maKhoaHoc, Pageable pageable) {
        return danhGiaKhoaHocKho.findByKhoaHoc_MaKhoaHocAndAnFalseOrderByNgayTaoDesc(maKhoaHoc, pageable)
                .map(this::sangPhanHoi);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PhanHoiDanhGia> layDanhGiaCuaToi(Long maNguoiDung, Long maKhoaHoc) {
        return danhGiaKhoaHocKho.findByMaNguoiDungAndKhoaHoc_MaKhoaHoc(maNguoiDung, maKhoaHoc).map(this::sangPhanHoi);
    }

    @Override
    public PhanHoiDanhGia anDanhGiaViPham(Long maNguoiDungAdmin, Long maDanhGia, YeuCauXuLyDanhGia yeuCau) {
        DanhGiaKhoaHoc danhGia = timHoacNem(maDanhGia);
        danhGia.anDoViPham(yeuCau.lyDo());

        thongBaoDichVu.guiThongBao(danhGia.getMaNguoiDung(), LoaiThongBao.DANH_GIA_BI_XU_LY,
                "Đánh giá của bạn đã bị ẩn",
                "Đánh giá của bạn cho khóa học \"" + danhGia.getKhoaHoc().getTieuDe()
                        + "\" đã bị Quản trị viên ẩn vì vi phạm tiêu chuẩn cộng đồng. Lý do: " + yeuCau.lyDo(),
                Map.of("maKhoaHoc", danhGia.getKhoaHoc().getMaKhoaHoc(), "maDanhGia", maDanhGia));

        return sangPhanHoi(danhGia);
    }

    @Override
    public void xoaDanhGiaViPham(Long maNguoiDungAdmin, Long maDanhGia, YeuCauXuLyDanhGia yeuCau) {
        DanhGiaKhoaHoc danhGia = timHoacNem(maDanhGia);
        Long maNguoiDungChuSoHuu = danhGia.getMaNguoiDung();
        String tieuDeKhoaHoc = danhGia.getKhoaHoc().getTieuDe();
        Long maKhoaHoc = danhGia.getKhoaHoc().getMaKhoaHoc();

        danhGiaKhoaHocKho.delete(danhGia);

        thongBaoDichVu.guiThongBao(maNguoiDungChuSoHuu, LoaiThongBao.DANH_GIA_BI_XU_LY,
                "Đánh giá của bạn đã bị xóa",
                "Đánh giá của bạn cho khóa học \"" + tieuDeKhoaHoc
                        + "\" đã bị Quản trị viên xóa vì vi phạm tiêu chuẩn cộng đồng. Lý do: " + yeuCau.lyDo(),
                Map.of("maKhoaHoc", maKhoaHoc));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiDanhGia> layTatCaDanhGiaChoAdmin(Long maKhoaHoc, Pageable pageable) {
        return danhGiaKhoaHocKho.findByKhoaHoc_MaKhoaHocOrderByNgayTaoDesc(maKhoaHoc, pageable)
                .map(this::sangPhanHoi);
    }

    @Override
    public PhanHoiDanhGia boAnDanhGia(Long maNguoiDungAdmin, Long maDanhGia) {
        DanhGiaKhoaHoc danhGia = timHoacNem(maDanhGia);
        danhGia.boHienThi();
        return sangPhanHoi(danhGia);
    }

    private DanhGiaKhoaHoc timHoacNem(Long maDanhGia) {
        return danhGiaKhoaHocKho.findById(maDanhGia)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("đánh giá", maDanhGia));
    }

    private void kiemTraQuyenSoHuu(DanhGiaKhoaHoc danhGia, Long maNguoiDung) {
        if (!danhGia.getMaNguoiDung().equals(maNguoiDung)) {
            throw new NgoaiLeKhongCoQuyen("Bạn không phải chủ sở hữu đánh giá này");
        }
    }

    private void kiemTraDuDieuKienDanhGia(Long maNguoiDung, Long maKhoaHoc) {
        boolean daSoHuu = quyenSuDungKho.existsByMaNguoiDungAndMaKhoaHoc(maNguoiDung, maKhoaHoc);
        if (!daSoHuu) {
            throw new NgoaiLeKhongCoQuyen(
                    "Bạn cần sở hữu khóa học này (đã mua hoặc đang tham gia lớp học dựa trên khóa học) mới có thể đánh giá");
        }

        // Nếu sở hữu thông qua một lớp học dựa trên khóa học này, yêu cầu thêm
        // tiến độ tối thiểu trong lớp đó. Nếu chỉ sở hữu qua tự học (chưa có cơ
        // chế theo dõi tiến độ tự học), điều kiện sở hữu ở trên đã là đủ.
        List<PhanHoiLop> lopDaThamGia = ghiDanhDichVu.layLopCuaHocVien(maNguoiDung);
        List<PhanHoiLop> lopTheoKhoaHocNay = lopDaThamGia.stream()
                .filter(lop -> lop.maKhoaHoc().equals(maKhoaHoc))
                .toList();

        if (!lopTheoKhoaHocNay.isEmpty()) {
            boolean datTienDoToiThieu = lopTheoKhoaHocNay.stream()
                    .anyMatch(lop -> lop.tiLeHoanThanh() != null && lop.tiLeHoanThanh() >= TIEN_DO_TOI_THIEU_PHAN_TRAM);
            if (!datTienDoToiThieu) {
                throw new NgoaiLeKhongCoQuyen(
                        "Bạn cần hoàn thành ít nhất " + TIEN_DO_TOI_THIEU_PHAN_TRAM
                                + "% nội dung lớp học trước khi đánh giá khóa học này");
            }
        }
    }

    private PhanHoiDanhGia sangPhanHoi(DanhGiaKhoaHoc d) {
        ThongTinNguoiDungCoBan nguoiDung = hoSoCongKhaiDichVu.layThongTinCoBan(d.getMaNguoiDung()).orElse(null);
        return new PhanHoiDanhGia(
                d.getMaDanhGia(),
                d.getMaNguoiDung(),
                nguoiDung != null ? nguoiDung.hoTen() : null,
                nguoiDung != null ? nguoiDung.anhDaiDien() : null,
                d.getSoSao(),
                d.getNhanXet(),
                d.getNgayTao(),
                d.getAn()
        );
    }
}