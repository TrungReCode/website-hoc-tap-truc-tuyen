package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi;

/**
 * FIX (Vấn đề #2): thay thế PhanHoiDangNhap làm response body công khai của
 * /dang-nhap và /lam-moi-token. accessToken/refreshToken KHÔNG còn xuất hiện
 * trong JSON trả về nữa — 2 giá trị đó giờ chỉ được gửi qua cookie HttpOnly
 * (xem CongCuCookie), JS phía frontend không đọc được nên không thể lưu vào
 * localStorage như trước. Các trường còn lại (hoTen, vaiTro...) không nhạy
 * cảm — frontend vẫn cần để hiển thị UI, và bản thân backend luôn tự kiểm
 * tra lại vai trò thật từ DB ở mọi endpoint nên các trường này chỉ phục vụ
 * hiển thị, không phải biên giới bảo mật.
 */
public record PhanHoiDangNhapCong(
        String hoTen,
        String email,
        String vaiTro,
        Long maNguoiDung
) {
    public static PhanHoiDangNhapCong tu(PhanHoiDangNhap phanHoiDayDu) {
        return new PhanHoiDangNhapCong(
                phanHoiDayDu.hoTen(),
                phanHoiDayDu.email(),
                phanHoiDayDu.vaiTro(),
                phanHoiDayDu.maNguoiDung());
    }
}