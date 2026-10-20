package com.elearning.choKhoaHoc.mienDomain.enumDomain;

/**
 * Khớp cau_hoi.loai_cau_hoi (VARCHAR(20), không phải Postgres native enum).
 *
 * Về mặt kiến trúc, không module nào khác CẦN biết loại câu hỏi cụ thể
 * (việc chấm điểm là logic nội bộ choKhoaHoc). Tuy nhiên enum vẫn phải
 * khai báo public: package "mienDomain.enumDomain" là sub-package khác
 * với "mienDomain" (nơi Entity CauHoi cư trú) — Java không cho phép
 * package-private xuyên sub-package, giống lý do Entity luôn phải public.
 */
public enum LoaiCauHoi {
    MOT_DAP_AN,    // chỉ 1 đáp án đúng (radio button)
    NHIEU_DAP_AN,  // nhiều đáp án đúng (checkbox)
    DUNG_SAI,      // câu hỏi Đúng/Sai
    TRA_LOI_NGAN,  // học viên gõ câu trả lời, so khớp với các đáp án giảng viên chấp nhận
    SAP_XEP,       // học viên sắp xếp các đáp án theo thứ tự đúng
    GHEP_CAP       // học viên ghép các cặp đáp án đúng
}
