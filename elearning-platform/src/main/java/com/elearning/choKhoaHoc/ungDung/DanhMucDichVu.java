package com.elearning.choKhoaHoc.ungDung;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhMuc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhMucQuanTri;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauCapNhatDanhMuc;
import com.elearning.choKhoaHoc.giaoDien.dto.yeuCau.YeuCauTaoDanhMuc;

import java.util.List;

public interface DanhMucDichVu {

    List<PhanHoiDanhMuc> layTatCa();

    /** Danh sách đầy đủ kèm số khóa học/danh mục con — dùng cho trang quản trị của QTV Nội dung. */
    List<PhanHoiDanhMucQuanTri> layTatCaChoQuanTri();

    PhanHoiDanhMucQuanTri taoDanhMuc(YeuCauTaoDanhMuc yeuCau);

    PhanHoiDanhMucQuanTri capNhatDanhMuc(Long maDanhMuc, YeuCauCapNhatDanhMuc yeuCau);

    /** Từ chối nếu danh mục còn khóa học hoặc còn danh mục con bên trong. */
    void xoaDanhMuc(Long maDanhMuc);
}