/**
 * Backend trả lỗi theo 2 dạng:
 * 1. PhanHoiLoi.java (lỗi nghiệp vụ) -> { thongBao, maLoi, ... }
 * 2. MethodArgumentNotValidException handler (lỗi @Valid) -> { thongBao, chiTiet: {...} }
 * Hàm này rút ra message phù hợp để hiển thị, không phân biệt 2 dạng trên.
 */
export function layThongBaoLoi(error) {
  const data = error?.response?.data;

  if (!data) {
    return 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra lại đường truyền.';
  }

  if (data.chiTiet) {
    // Lỗi validate field cụ thể — lấy message đầu tiên cho gọn
    const truongDauTien = Object.values(data.chiTiet)[0];
    return truongDauTien || data.thongBao || 'Dữ liệu không hợp lệ.';
  }

  return data.thongBao || 'Đã có lỗi xảy ra, vui lòng thử lại.';
}
