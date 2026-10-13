package com.elearning.dinhDanhPhanQuyen.ungDung.impl;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiXacThucGiangVien;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDuyetXacThucGiangVien;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauXacThucGiangVien;
import com.elearning.dinhDanhPhanQuyen.haTang.NguoiDungKho;
import com.elearning.dinhDanhPhanQuyen.haTang.XacThucGiangVienKho;
import com.elearning.dinhDanhPhanQuyen.haTang.boPhanNgoai.AdapterLuuTruHoSoXacThuc;
import com.elearning.dinhDanhPhanQuyen.mienDomain.NguoiDung;
import com.elearning.dinhDanhPhanQuyen.mienDomain.XacThucGiangVien;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.TrangThaiXacThucGiangVien;
import com.elearning.dinhDanhPhanQuyen.ungDung.XacThucGiangVienDichVu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
class XacThucGiangVienDichVuImpl implements XacThucGiangVienDichVu {

    private static final String THU_MUC_LUU_TRU = "dinh-danh/xac-thuc-giang-vien";

    private final XacThucGiangVienKho xacThucGiangVienKho;
    private final NguoiDungKho nguoiDungKho;
    private final AdapterLuuTruHoSoXacThuc adapterLuuTruHoSoXacThuc;

    @Override
    // FIX (cùng lý do đã sửa ở QuanLyNoiDungDichVuImpl.themBaiHoc): upload
    // file có thể mất vài giây/phút với file lớn/mạng chậm — tạm ngưng
    // transaction ghi CSDL trong suốt lúc upload, tránh giữ connection CSDL
    // không cần thiết. save() bên dưới vẫn an toàn vì JpaRepository.save()
    // tự có transaction riêng cho từng lệnh.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PhanHoiXacThucGiangVien guiYeuCau(Long maNguoiDung, YeuCauXacThucGiangVien yeuCau,
            MultipartFile fileMinhChung) {
        if (yeuCau.ngaySinh() == null) {
            throw new NgoaiLeDuLieuKhongHopLe("Vui lòng nhập ngày sinh");
        }
        if (yeuCau.bangCap() == null || yeuCau.bangCap().isBlank()) {
            throw new NgoaiLeDuLieuKhongHopLe("Vui lòng khai báo bằng cấp/chứng chỉ chuyên môn");
        }
        if (fileMinhChung == null || fileMinhChung.isEmpty()) {
            throw new NgoaiLeDuLieuKhongHopLe("Vui lòng đính kèm file minh chứng (bằng cấp/CCCD)");
        }
        // Kiểm tra tuổi ngay tại đây (trước khi tốn công upload file) — dùng
        // Period thay vì phép cộng năm thô để tính đúng cả năm nhuận.
        int tuoi = Period.between(yeuCau.ngaySinh(), LocalDate.now()).getYears();
        if (tuoi < 18) {
            throw new NgoaiLeDuLieuKhongHopLe("Phải đủ 18 tuổi trở lên mới được đăng ký trở thành giảng viên");
        }

        Optional<XacThucGiangVien> daCoOpt = xacThucGiangVienKho.findByMaNguoiDung(maNguoiDung);
        if (daCoOpt.isPresent() && daCoOpt.get().getTrangThai() != TrangThaiXacThucGiangVien.TU_CHOI) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    daCoOpt.get().getTrangThai() == TrangThaiXacThucGiangVien.CHO_DUYET
                            ? "Bạn đã có yêu cầu đang chờ duyệt, vui lòng đợi Quản trị viên xử lý"
                            : "Bạn đã được xác thực là giảng viên, không cần gửi lại yêu cầu");
        }

        // FIX: lưu lại đường dẫn file CŨ trước khi bị ghi đè bên dưới — nếu
        // đây là lần gửi lại (đã từng bị TU_CHOI), file cũ sẽ bị xóa khỏi R2
        // SAU KHI lưu DB thành công (xem cuối hàm), tránh để lại file mồ côi.
        String duongDanCu = daCoOpt.map(XacThucGiangVien::getDuongDanLuuTru).orElse(null);

        String duongDan = adapterLuuTruHoSoXacThuc.taiLenFileRiengTu(fileMinhChung, THU_MUC_LUU_TRU);

        XacThucGiangVien ketQua;
        if (daCoOpt.isPresent()) {
            // Đã từng bị TU_CHOI — gửi lại là UPDATE bản ghi cũ, không tạo mới.
            ketQua = daCoOpt.get();
            ketQua.guiLai(yeuCau.ngaySinh(), yeuCau.bangCap(), duongDan, fileMinhChung.getOriginalFilename());
        } else {
            ketQua = new XacThucGiangVien(maNguoiDung, yeuCau.ngaySinh(), yeuCau.bangCap(), duongDan,
                    fileMinhChung.getOriginalFilename());
        }
        xacThucGiangVienKho.save(ketQua);

        // Xóa file cũ CHỈ sau khi bản ghi mới đã lưu thành công — nếu làm
        // ngược lại (xóa trước, lưu sau) mà save() thất bại thì sẽ mất cả
        // file cũ lẫn file mới, không còn minh chứng nào để admin xem lại.
        if (duongDanCu != null) {
            adapterLuuTruHoSoXacThuc.xoa(duongDanCu);
        }

        return sangDto(ketQua, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PhanHoiXacThucGiangVien> layTrangThaiCuaToi(Long maNguoiDung) {
        return xacThucGiangVienKho.findByMaNguoiDung(maNguoiDung).map(x -> sangDto(x, null));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean daDuocXacThuc(Long maNguoiDung) {
        return xacThucGiangVienKho.existsByMaNguoiDungAndTrangThai(maNguoiDung, TrangThaiXacThucGiangVien.DA_DUYET);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiXacThucGiangVien> layDanhSachChoAdmin(String trangThai, Pageable pageable) {
        TrangThaiXacThucGiangVien tt;
        try {
            tt = trangThai == null || trangThai.isBlank()
                    ? TrangThaiXacThucGiangVien.CHO_DUYET
                    : TrangThaiXacThucGiangVien.valueOf(trangThai);
        } catch (IllegalArgumentException ex) {
            throw new NgoaiLeDuLieuKhongHopLe("Trạng thái không hợp lệ: " + trangThai);
        }
        return xacThucGiangVienKho.findByTrangThai(tt, pageable).map(x -> {
            NguoiDung nd = nguoiDungKho.findById(x.getMaNguoiDung()).orElse(null);
            return sangDto(x, nd);
        });
    }

    @Override
    public PhanHoiXacThucGiangVien duyetYeuCau(Long maNguoiDungAdmin, Long maXacThuc,
            YeuCauDuyetXacThucGiangVien yeuCau) {
        XacThucGiangVien x = xacThucGiangVienKho.findById(maXacThuc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("yêu cầu xác thực giảng viên", maXacThuc));

        if (x.getTrangThai() != TrangThaiXacThucGiangVien.CHO_DUYET) {
            throw new NgoaiLeDuLieuKhongHopLe("Yêu cầu này không ở trạng thái chờ duyệt");
        }
        if (!yeuCau.chapDuyet() && (yeuCau.lyDoTuChoi() == null || yeuCau.lyDoTuChoi().isBlank())) {
            throw new NgoaiLeDuLieuKhongHopLe("Vui lòng nhập lý do khi từ chối yêu cầu");
        }

        x.setTrangThai(yeuCau.chapDuyet() ? TrangThaiXacThucGiangVien.DA_DUYET : TrangThaiXacThucGiangVien.TU_CHOI);
        x.setLyDoTuChoi(yeuCau.chapDuyet() ? null : yeuCau.lyDoTuChoi().trim());
        x.setMaAdminXuLy(maNguoiDungAdmin);
        x.setNgayXuLy(LocalDateTime.now());
        xacThucGiangVienKho.save(x);

        NguoiDung nd = nguoiDungKho.findById(x.getMaNguoiDung()).orElse(null);
        return sangDto(x, nd);
    }

    @Override
    @Transactional(readOnly = true)
    public KetQuaTaiFileXacThuc taiFileMinhChung(Long maNguoiDungAdmin, Long maXacThuc) {
        // Quyền admin đã được chặn ở @PreAuthorize cấp Controller — ở đây
        // chỉ cần tìm đúng bản ghi, không cần kiểm tra thêm điều kiện gì
        // khác (mọi QTV nội dung/cao cấp đều được xem mọi hồ sơ xác thực).
        XacThucGiangVien x = xacThucGiangVienKho.findById(maXacThuc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("yêu cầu xác thực giảng viên", maXacThuc));
        byte[] noiDung = adapterLuuTruHoSoXacThuc.taiFileRiengTu(x.getDuongDanLuuTru());
        return new KetQuaTaiFileXacThuc(noiDung, x.getTenFileGoc());
    }

    private PhanHoiXacThucGiangVien sangDto(XacThucGiangVien x, NguoiDung nd) {
        int tuoi = Period.between(x.getNgaySinh(), LocalDate.now()).getYears();
        return new PhanHoiXacThucGiangVien(
                x.getMaXacThuc(), x.getMaNguoiDung(),
                nd != null ? nd.getHoTen() : null, nd != null ? nd.getEmail() : null,
                x.getNgaySinh(), tuoi, x.getBangCap(), x.getTenFileGoc(),
                x.getTrangThai(), x.getLyDoTuChoi(), x.getNgayGui(), x.getNgayXuLy());
    }
}