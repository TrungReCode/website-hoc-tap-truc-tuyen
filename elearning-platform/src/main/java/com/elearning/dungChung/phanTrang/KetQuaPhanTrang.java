package com.elearning.dungChung.phanTrang;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper phân trang chuẩn cho toàn hệ thống — mọi API trả về danh sách
 * (danh sách khóa học, danh sách học viên, danh sách thông báo...) đều
 * dùng cấu trúc này để Frontend xử lý đồng nhất, không cần biết Spring
 * Page<T> trả về định dạng gì.
 */
public record KetQuaPhanTrang<T>(
        List<T> noiDung,
        int trangHienTai,
        int kichThuocTrang,
        long tongSoPhanTu,
        int tongSoTrang,
        boolean laTrangCuoi
) {
    public static <T> KetQuaPhanTrang<T> tuPage(Page<T> page) {
        return new KetQuaPhanTrang<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
