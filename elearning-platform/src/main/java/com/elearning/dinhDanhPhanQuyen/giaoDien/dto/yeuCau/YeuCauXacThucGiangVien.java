package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau;

import java.time.LocalDate;

/**
 * File minh chứng (bằng cấp/CCCD) gửi kèm dưới dạng MultipartFile riêng ở
 * tầng Controller (multipart/form-data), không nằm trong record này — theo
 * đúng quy ước đã dùng ở YeuCauTaoBaiHoc (choKhoaHoc).
 */
public record YeuCauXacThucGiangVien(
        LocalDate ngaySinh,
        String bangCap
) {
}
