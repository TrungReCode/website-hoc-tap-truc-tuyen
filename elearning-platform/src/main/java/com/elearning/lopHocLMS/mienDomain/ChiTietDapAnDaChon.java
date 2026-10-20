package com.elearning.lopHocLMS.mienDomain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Khớp cấu trúc chi_tiet_dap_an JSONB: [{question_id, selected_answer_ids[], is_correct}].
 * traLoiVanBan / capGhep chỉ có với câu TRA_LOI_NGAN / GHEP_CAP; NON_NULL để kết quả
 * của các dạng cũ lưu y hệt như trước, và kết quả đã lưu từ trước vẫn đọc lên bình thường.
 */
public record ChiTietDapAnDaChon(Long maCauHoi, List<Integer> dsMaDapAnDaChon, Boolean dapAnDung,
        @JsonInclude(JsonInclude.Include.NON_NULL) String traLoiVanBan,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<CapGhepDaNoi> capGhep) {
}
