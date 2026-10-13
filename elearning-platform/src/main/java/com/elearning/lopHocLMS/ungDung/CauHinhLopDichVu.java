package com.elearning.lopHocLMS.ungDung;

import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCapNhatLop;

public interface CauHinhLopDichVu {

    PhanHoiLop capNhatCauHinh(Long maNguoiDung, Long maLopHoc, YeuCauCapNhatLop yeuCau);

    /** SAP_DIEN_RA/HOAT_DONG -> DA_DONG. Không thể hoàn tác. */
    PhanHoiLop dongLop(Long maNguoiDung, Long maLopHoc);
}
