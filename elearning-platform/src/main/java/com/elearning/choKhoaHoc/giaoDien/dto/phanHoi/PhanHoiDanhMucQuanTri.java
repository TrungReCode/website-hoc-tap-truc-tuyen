package com.elearning.choKhoaHoc.giaoDien.dto.phanHoi;

public record PhanHoiDanhMucQuanTri(
        Long maDanhMuc,
        String tenDanhMuc,
        Long maDanhMucCha,
        String tenDanhMucCha,
        long soKhoaHoc,
        long soDanhMucCon
) {
}