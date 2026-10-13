package com.elearning.choKhoaHoc.mienDomain;

/**
 * Một phương án trả lời trong JSONB cau_hoi.dap_an, ví dụ:
 * [{"id":1,"noiDung":"Spring Boot là gì?","dapAnDung":true}, ...]
 *
 * dapAnDung KHÔNG được trả về phía Học viên khi đang làm bài (chỉ tầng
 * chấm điểm ở module lopHocLMS được đọc); trách nhiệm ẩn trường này khi
 * trả DTO ra ngoài thuộc về PhanHoiCauHoi ở giaoDien/dto/phanHoi.
 */
public record DapAnCauHoi(Integer id, String noiDung, Boolean dapAnDung) {
}
