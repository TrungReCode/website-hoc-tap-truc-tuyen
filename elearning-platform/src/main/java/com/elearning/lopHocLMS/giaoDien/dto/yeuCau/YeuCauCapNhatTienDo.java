package com.elearning.lopHocLMS.giaoDien.dto.yeuCau;

public record YeuCauCapNhatTienDo(
        Long viTriDung, // giây video / số trang — null nếu không áp dụng
        Boolean hoanThanh // true để chủ động đánh dấu hoàn thành; null/false để chỉ cập nhật vị trí dừng
) {
}
