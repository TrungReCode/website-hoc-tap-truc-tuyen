package com.elearning.lopHocLMS.mienDomain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ThanhVienLopId implements Serializable {
    private Long maHocVien;
    private Long maLopHoc;
}
