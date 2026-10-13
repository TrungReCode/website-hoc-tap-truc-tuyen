package com.elearning.lopHocLMS.mienDomain;

import java.util.List;

/** Khớp cấu trúc chi_tiet_dap_an JSONB: [{question_id, selected_answer_ids[], is_correct}]. */
public record ChiTietDapAnDaChon(Long maCauHoi, List<Integer> dsMaDapAnDaChon, Boolean dapAnDung) {
}
