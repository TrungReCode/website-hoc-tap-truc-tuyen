package com.elearning.lopHocLMS.mienDomain.enumDomain;

/**
 * Khớp bai_hoc_bo_sung.loai (VARCHAR(20)). LINK không cần tải tệp, chỉ
 * lưu URL trực tiếp. Bổ sung QUIZ (CSDL.md không liệt kê nhưng cột là
 * varchar(20) tự do) — cần thiết để có đường dẫn tạo bai_kiem_tra_bo_sung,
 * vì bảng đó bắt buộc liên kết 1-1 với một bài học bổ sung.
 */
public enum LoaiHocLieuBoSung {
    VIDEO,
    PDF,
    DOC,
    LINK,
    ZIP,
    QUIZ
}
