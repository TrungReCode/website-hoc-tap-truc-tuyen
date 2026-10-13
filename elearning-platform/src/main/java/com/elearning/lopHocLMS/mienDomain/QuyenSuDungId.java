package com.elearning.lopHocLMS.mienDomain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class QuyenSuDungId implements Serializable {
    private Long maNguoiDung;
    private Long maKhoaHoc;
}
