package com.elearning.lopHocLMS.mienDomain;

import com.fasterxml.jackson.annotation.JsonInclude;

/** noiDungGhep: chỉ dùng cho câu GHEP_CAP — xem DapAnCauHoi và QuyTacCauHoi. */
public record DapAnBoSung(Integer id, String noiDung, Boolean dapAnDung,
        @JsonInclude(JsonInclude.Include.NON_NULL) String noiDungGhep) {
}
