package com.elearning.traoDoiTrongLop.mienDomain.enumDomain;

/**
 * Phân loại thông báo trong hệ thống.
 * BAI_DANG_MOI            : có bài đăng mới trong lớp → gửi cho toàn thành viên.
 * BINH_LUAN_MOI           : có người reply bình luận → gửi cho chủ bài đăng.
 * GIAI_DAP                : giảng viên đánh dấu giải đáp → gửi cho người đặt câu hỏi.
 * THANH_TOAN_THANH_CONG   : thanh toán khóa học/lớp học thành công → gửi cho người mua.
 * HOAN_TIEN_THANH_CONG    : yêu cầu hoàn tiền đã được xử lý thành công qua VNPay → gửi cho người yêu cầu.
 * HOAN_TIEN_TU_CHOI       : yêu cầu hoàn tiền bị QTV tài chính từ chối → gửi cho người yêu cầu.
 * KHOA_HOC_DA_DUYET       : QTV nội dung duyệt phát hành khóa học → gửi cho tác giả.
 * KHOA_HOC_BI_TU_CHOI     : QTV nội dung từ chối khóa học → gửi cho tác giả kèm lý do.
 * KHOA_HOC_YEU_CAU_BO_SUNG: QTV nội dung yêu cầu bổ sung → gửi cho tác giả kèm ghi chú.
 * LOP_HOC_DA_HUY          : giảng viên hủy lớp chưa khai giảng → gửi cho học viên đã đóng phí.
 * DANH_GIA_BI_XU_LY       : QTV nội dung ẩn/xóa đánh giá vi phạm → gửi cho học viên viết đánh giá.
 * TAI_KHOAN_BI_KHOA       : QTV khóa tài khoản vi phạm → gửi cho chủ tài khoản kèm lý do.
 * DOI_SOAT_HOAN_TAT       : QTV tài chính xác nhận đối soát hoàn tất, đã chuyển tiền → gửi cho giảng viên.
 * KHOA_HOC_CHO_DUYET      : giảng viên gửi khóa học đi kiểm duyệt → gửi cho toàn bộ QTV_NOI_DUNG.
 * HOAN_TIEN_MOI           : có yêu cầu hoàn tiền mới cần xử lý → gửi cho toàn bộ QTV_TAI_CHINH.
 * DOI_SOAT_MOI            : có phiếu đối soát mới (tự động gom theo lô) → gửi cho toàn bộ QTV_TAI_CHINH.
 */
public enum LoaiThongBao {
    BAI_DANG_MOI,
    BINH_LUAN_MOI,
    GIAI_DAP,
    THANH_TOAN_THANH_CONG,
    HOAN_TIEN_THANH_CONG,
    HOAN_TIEN_TU_CHOI,
    KHOA_HOC_DA_DUYET,
    KHOA_HOC_BI_TU_CHOI,
    KHOA_HOC_YEU_CAU_BO_SUNG,
    LOP_HOC_DA_HUY,
    DANH_GIA_BI_XU_LY,
    TAI_KHOAN_BI_KHOA,
    DOI_SOAT_HOAN_TAT,
    KHOA_HOC_CHO_DUYET,
    HOAN_TIEN_MOI,
    DOI_SOAT_MOI
}