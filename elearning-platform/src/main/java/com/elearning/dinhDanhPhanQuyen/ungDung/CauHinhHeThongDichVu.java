package com.elearning.dinhDanhPhanQuyen.ungDung;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiCauHinhHeThong;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauCapNhatCauHinhHeThong;

/**
 * PUBLIC — UC-13: Quản trị viên cấp cao điều chỉnh thông số toàn cục (tỷ lệ
 * phí dịch vụ, thời gian giữ ký quỹ trước khi đối soát).
 *
 * Lưu trữ trên Redis (hash "he-thong:cau-hinh") thay vì bảng CSDL mới — theo
 * đúng yêu cầu không chỉnh sửa schema Postgres. Khi Redis chưa có giá trị
 * nào (lần đầu chạy), layCauHinh() trả về đúng các giá trị mặc định đang
 * hardcode rải rác trong module thanhToan trước đây, đảm bảo hành vi hệ
 * thống không đổi cho tới khi Quản trị viên chủ động chỉnh sửa.
 *
 * Các module khác (thanhToan) gọi layCauHinh() thay vì dùng hằng số riêng.
 */
public interface CauHinhHeThongDichVu {

    PhanHoiCauHinhHeThong layCauHinh();

    PhanHoiCauHinhHeThong capNhatCauHinh(Long maNguoiDungCaoCap, YeuCauCapNhatCauHinhHeThong yeuCau);
}
