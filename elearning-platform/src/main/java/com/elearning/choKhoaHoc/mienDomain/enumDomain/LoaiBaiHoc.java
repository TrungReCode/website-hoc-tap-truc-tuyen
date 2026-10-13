package com.elearning.choKhoaHoc.mienDomain.enumDomain;

/**
 * Cột bai_hoc.loai là VARCHAR(20) tự do (không phải Postgres native enum),
 * nên enum này chỉ ràng buộc ở tầng Java, ánh xạ bằng EnumType.STRING.
 *
 * CSDL.md mô tả 3 giá trị gốc (VIDEO, PDF, DOC); bổ sung thêm QUIZ vì màn
 * hình "Tải lên video/tài liệu" (S23) trong mockup có lựa chọn loại bài
 * học là "QUIZ (Bài kiểm tra)" — cột varchar(20) cho phép mở rộng này mà
 * không cần sửa migration đã áp dụng.
 *
 * PUBLIC vì module lopHocLMS cần biết loại bài học để chọn đúng trình phát
 * (video player / PDF viewer / giao diện làm bài kiểm tra) khi học viên
 * học trong lớp.
 */
public enum LoaiBaiHoc {
    VIDEO,
    PDF,
    DOC,
    QUIZ
}
