package com.elearning.dinhDanhPhanQuyen.ungDung.impl;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiCauHinhHeThong;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauCapNhatCauHinhHeThong;
import com.elearning.dinhDanhPhanQuyen.ungDung.CauHinhHeThongDichVu;
import com.elearning.dungChung.nhatKy.GhiNhatKyHanhDong;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
class CauHinhHeThongDichVuImpl implements CauHinhHeThongDichVu {

    private static final String KHOA_CAU_HINH = "he-thong:cau-hinh";

    // Giữ đúng các giá trị đã hardcode trước đây trong module thanhToan, để
    // hành vi hệ thống không đổi cho tới khi Quản trị viên chủ động chỉnh sửa.
    private static final BigDecimal MAC_DINH_TY_LE_PHI_CA_NHAN = new BigDecimal("10.00");
    private static final BigDecimal MAC_DINH_TY_LE_PHI_DAY_HOC = new BigDecimal("10.00");
    private static final int MAC_DINH_SO_NGAY_KY_QUY_KHOA_HOC = 7;
    private static final int MAC_DINH_SO_NGAY_KY_QUY_LOP_HOC = 7;

    private final RedisTemplate<String, Object> redisTemplate;
    private final GhiNhatKyHanhDong ghiNhatKyHanhDong;
    private final ObjectMapper objectMapper;

    @Override
    public PhanHoiCauHinhHeThong layCauHinh() {
        Object gt = redisTemplate.opsForValue().get(KHOA_CAU_HINH);
        if (gt == null) {
            return macDinh();
        }
        if (gt instanceof PhanHoiCauHinhHeThong cauHinh) {
            return cauHinh;
        }

        if (gt instanceof java.util.Map<?, ?> map && map.containsKey("tyLePhiDichVu")
                && !map.containsKey("tyLePhiCaNhan")) {
            BigDecimal tyLeCu = objectMapper.convertValue(map.get("tyLePhiDichVu"), BigDecimal.class);
            Integer soNgayKhoaHoc = objectMapper.convertValue(map.get("soNgayGiuKyQuyKhoaHoc"), Integer.class);
            Integer soNgayLopHoc = objectMapper.convertValue(map.get("soNgayGiuKyQuyLopHoc"), Integer.class);
            return new PhanHoiCauHinhHeThong(tyLeCu, tyLeCu, soNgayKhoaHoc, soNgayLopHoc);
        }
        return objectMapper.convertValue(gt, PhanHoiCauHinhHeThong.class);
    }

    @Override
    public PhanHoiCauHinhHeThong capNhatCauHinh(Long maNguoiDungCaoCap, YeuCauCapNhatCauHinhHeThong yeuCau) {
        PhanHoiCauHinhHeThong cauHinhCu = layCauHinh();

        PhanHoiCauHinhHeThong cauHinhMoi = new PhanHoiCauHinhHeThong(
                yeuCau.tyLePhiCaNhan(), yeuCau.tyLePhiDayHoc(),
                yeuCau.soNgayGiuKyQuyKhoaHoc(), yeuCau.soNgayGiuKyQuyLopHoc());

        // Không đặt TTL — cấu hình phải tồn tại vĩnh viễn cho tới khi bị ghi
        // đè bởi lần cập nhật kế tiếp (khác với các khóa Redis khác trong dự
        // án dùng làm distributed lock tạm thời).
        redisTemplate.opsForValue().set(KHOA_CAU_HINH, cauHinhMoi);

        ghiNhatKyHanhDong.ghiNhan(maNguoiDungCaoCap, "CAP_NHAT_CAU_HINH_HE_THONG", "he-thong:cau-hinh",
                "Cũ: " + cauHinhCu + " -> Mới: " + cauHinhMoi);

        return cauHinhMoi;
    }

    private PhanHoiCauHinhHeThong macDinh() {
        return new PhanHoiCauHinhHeThong(
                MAC_DINH_TY_LE_PHI_CA_NHAN, MAC_DINH_TY_LE_PHI_DAY_HOC,
                MAC_DINH_SO_NGAY_KY_QUY_KHOA_HOC, MAC_DINH_SO_NGAY_KY_QUY_LOP_HOC);
    }
}