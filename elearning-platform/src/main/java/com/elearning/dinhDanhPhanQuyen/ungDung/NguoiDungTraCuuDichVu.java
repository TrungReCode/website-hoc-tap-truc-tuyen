package com.elearning.dinhDanhPhanQuyen.ungDung;

import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.VaiTro;

import java.util.List;

/**
 * PUBLIC — Điểm vào duy nhất để các module khác (choKhoaHoc, thanhToan...)
 * tra cứu người dùng theo vai trò mà KHÔNG cần import trực tiếp NguoiDungKho
 * (bị cấm bởi quy ước ranh giới module — xem javadoc NguoiDungKho).
 *
 * Trường hợp dùng chính: gửi thông báo hàng loạt cho toàn bộ quản trị viên
 * đúng vai trò khi phát sinh việc cần họ xử lý (khóa học chờ duyệt, yêu cầu
 * hoàn tiền mới, phiếu đối soát mới...).
 */
public interface NguoiDungTraCuuDichVu {

    /** Chỉ trả về mã của tài khoản đang HOAT_DONG. */
    List<Long> layDanhSachMaNguoiDungTheoVaiTro(VaiTro vaiTro);
}