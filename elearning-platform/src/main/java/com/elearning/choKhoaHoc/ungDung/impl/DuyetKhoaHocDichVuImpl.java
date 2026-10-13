package com.elearning.choKhoaHoc.ungDung.impl;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhSachKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.HanhDongDuyet;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauDuyetKhoaHoc;
import com.elearning.choKhoaHoc.haTang.KhoaHocKho;
import com.elearning.choKhoaHoc.mienDomain.KhoaHoc;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.TrangThaiKhoaHoc;
import com.elearning.choKhoaHoc.ungDung.DuyetKhoaHocDichVu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.dungChung.ngoaiLe.NgoaiLeNghiepVu;
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
import java.util.Map;

/**
 * UC-11 Kiểm duyệt nội dung. Khóa Redis dùng SETNX (RedisTemplate.opsForValue().setIfAbsent)
 * với key "kiem-duyet-khoa-hoc:lock:{maKhoaHoc}", giá trị là mã Admin đang
 * giữ khóa, TTL 10 phút (tự nhả nếu Admin bỏ dở không quyết định và không
 * tắt trình duyệt đúng cách).
 */
@Service
@RequiredArgsConstructor
@Transactional
class DuyetKhoaHocDichVuImpl implements DuyetKhoaHocDichVu {

    private static final String TIEN_TO_KHOA = "kiem-duyet-khoa-hoc:lock:";
    private static final Duration THOI_GIAN_GIU_KHOA = Duration.ofMinutes(10);

    private final KhoaHocKho khoaHocKho;
    private final KhoaHocMapper khoaHocMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ThongBaoDichVu thongBaoDichVu;

    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiDanhSachKhoaHoc> layDanhSachQuanLy(TrangThaiKhoaHoc trangThai, String tuKhoa, Pageable pageable) {
        String tenTrangThai = trangThai == null ? null : trangThai.name();
        String tuKhoaSach = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        return khoaHocKho.timKiemChoAdmin(tenTrangThai, tuKhoaSach, pageable)
                .map(khoaHocMapper::sangPhanHoiDanhSach);
    }

    @Override
    @Transactional(readOnly = true)
    public PhanHoiKhoaHoc moHoSoKiemDuyet(Long maNguoiDungAdmin, Long maKhoaHoc) {
        KhoaHoc khoaHoc = khoaHocKho.findById(maKhoaHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("khóa học", maKhoaHoc));

        // FIX: trước đây chặn cứng, Admin không xem lại được nội dung khóa
        // học đã CÔNG_KHAI hoặc đã LƯU_TRỮ từ trang kiểm duyệt (VD: có báo
        // cáo vi phạm cần kiểm tra lại nội dung đã duyệt trước đó). Khóa
        // Redis (chống 2 Admin cùng xét duyệt 1 hồ sơ) chỉ còn ý nghĩa khi
        // hồ sơ THỰC SỰ đang chờ quyết định — bỏ qua hoàn toàn cho các
        // trạng thái khác, vì xem lại nội dung đã xong không có gì để
        // "giành nhau" quyết định cả.
        if (khoaHoc.getTrangThai() != TrangThaiKhoaHoc.CHO_KIEM_DUYET) {
            return khoaHocMapper.sangPhanHoiChiTiet(khoaHoc);
        }

        String khoa = TIEN_TO_KHOA + maKhoaHoc;
        Object nguoiDangGiu = redisTemplate.opsForValue().get(khoa);

        if (nguoiDangGiu != null && !nguoiDangGiu.toString().equals(maNguoiDungAdmin.toString())) {
            throw new NgoaiLeNghiepVu(
                    "Hồ sơ này đang được một Quản trị viên khác xét duyệt, vui lòng thử lại sau ít phút",
                    HttpStatus.CONFLICT, "HO_SO_DANG_DUOC_XET_DUYET");
        }

        Boolean chiemDuocKhoa = redisTemplate.opsForValue()
                .setIfAbsent(khoa, maNguoiDungAdmin.toString(), THOI_GIAN_GIU_KHOA);
        // setIfAbsent trả về false nếu khóa đã tồn tại do CHÍNH Admin này giữ trước đó (mở lại) -> vẫn hợp lệ, chỉ gia hạn TTL
        if (Boolean.FALSE.equals(chiemDuocKhoa)) {
            redisTemplate.expire(khoa, THOI_GIAN_GIU_KHOA);
        }

        return khoaHocMapper.sangPhanHoiChiTiet(khoaHoc);
    }

    @Override
    public void dongHoSoKiemDuyet(Long maNguoiDungAdmin, Long maKhoaHoc) {
        String khoa = TIEN_TO_KHOA + maKhoaHoc;
        Object nguoiDangGiu = redisTemplate.opsForValue().get(khoa);
        if (nguoiDangGiu != null && nguoiDangGiu.toString().equals(maNguoiDungAdmin.toString())) {
            redisTemplate.delete(khoa);
        }
    }

    @Override
    public PhanHoiKhoaHoc duyetKhoaHoc(Long maNguoiDungAdmin, Long maKhoaHoc, YeuCauDuyetKhoaHoc yeuCau) {
        KhoaHoc khoaHoc = khoaHocKho.findById(maKhoaHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("khóa học", maKhoaHoc));

        if (khoaHoc.getTrangThai() != TrangThaiKhoaHoc.CHO_KIEM_DUYET) {
            throw new NgoaiLeDuLieuKhongHopLe("Khóa học này hiện không ở trạng thái chờ duyệt");
        }

        String khoa = TIEN_TO_KHOA + maKhoaHoc;
        Object nguoiDangGiu = redisTemplate.opsForValue().get(khoa);
        if (nguoiDangGiu == null || !nguoiDangGiu.toString().equals(maNguoiDungAdmin.toString())) {
            throw new NgoaiLeNghiepVu(
                    "Bạn cần mở hồ sơ này trước khi đưa ra quyết định (hồ sơ có thể đã hết hạn giữ chỗ)",
                    HttpStatus.CONFLICT, "CHUA_GIU_HO_SO");
        }

        if (yeuCau.hanhDong() == HanhDongDuyet.DUYET) {
            khoaHoc.setTrangThai(TrangThaiKhoaHoc.CONG_KHAI);
            khoaHoc.setLyDoTuChoi(null);

            thongBaoDichVu.guiThongBao(khoaHoc.getMaTacGia(), LoaiThongBao.KHOA_HOC_DA_DUYET,
                    "Khóa học đã được duyệt phát hành",
                    "Chúc mừng! Khóa học \"" + khoaHoc.getTieuDe() + "\" đã được duyệt và hiện đang công khai trên Chợ khóa học.",
                    Map.of("maKhoaHoc", khoaHoc.getMaKhoaHoc()));

        } else if (yeuCau.hanhDong() == HanhDongDuyet.TU_CHOI) {
            if (yeuCau.ghiChu() == null || yeuCau.ghiChu().isBlank()) {
                throw new NgoaiLeDuLieuKhongHopLe("Vui lòng nhập lý do từ chối để Giảng viên biết cách chỉnh sửa");
            }
            khoaHoc.setTrangThai(TrangThaiKhoaHoc.NHAP);
            khoaHoc.setLyDoTuChoi(yeuCau.ghiChu());

            thongBaoDichVu.guiThongBao(khoaHoc.getMaTacGia(), LoaiThongBao.KHOA_HOC_BI_TU_CHOI,
                    "Khóa học bị từ chối duyệt",
                    "Khóa học \"" + khoaHoc.getTieuDe() + "\" đã bị từ chối. Lý do: " + yeuCau.ghiChu(),
                    Map.of("maKhoaHoc", khoaHoc.getMaKhoaHoc()));

        } else { // YEU_CAU_BO_SUNG
            if (yeuCau.ghiChu() == null || yeuCau.ghiChu().isBlank()) {
                throw new NgoaiLeDuLieuKhongHopLe("Vui lòng nhập ghi chú để Giảng viên biết cần bổ sung những gì");
            }
            khoaHoc.setTrangThai(TrangThaiKhoaHoc.CHO_BO_SUNG);
            khoaHoc.setLyDoTuChoi(yeuCau.ghiChu());

            thongBaoDichVu.guiThongBao(khoaHoc.getMaTacGia(), LoaiThongBao.KHOA_HOC_YEU_CAU_BO_SUNG,
                    "Khóa học cần bổ sung thêm thông tin",
                    "Quản trị viên yêu cầu bổ sung cho khóa học \"" + khoaHoc.getTieuDe() + "\": " + yeuCau.ghiChu()
                            + ". Sau khi chỉnh sửa, vui lòng gửi lại để tiếp tục xét duyệt.",
                    Map.of("maKhoaHoc", khoaHoc.getMaKhoaHoc()));
        }
        khoaHoc.danhDauCapNhat();

        redisTemplate.delete(khoa);

        return khoaHocMapper.sangPhanHoiChiTiet(khoaHoc);
    }
}