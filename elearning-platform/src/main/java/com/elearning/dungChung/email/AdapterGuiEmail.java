package com.elearning.dungChung.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Adapter gửi email dùng chung toàn hệ thống — đặt ở dungChung vì không
 * chỉ module IAM cần gửi email (về sau ThongBaoDichVu hoặc module
 * thanhToan cũng có thể cần gửi email xác nhận giao dịch).
 *
 * @Async để không block luồng HTTP chính lúc đăng ký/đăng nhập —
 * cần @EnableAsync đã có sẵn ở CauHinhAsync.
 */
@Component
@RequiredArgsConstructor
public class AdapterGuiEmail {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String diaChiGui;

    @Value("${app.frontend-url}")
    private String urlFrontend;

    @Async
    public void guiEmailKichHoat(String emailNguoiNhan, String hoTen, String token) {
        String duongDanKichHoat = urlFrontend + "/kich-hoat?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(diaChiGui);
        message.setTo(emailNguoiNhan);
        message.setSubject("Kích hoạt tài khoản - Nền Tảng Học Tập Trực Tuyến");
        message.setText(
                "Chào " + hoTen + ",\n\n" +
                "Cảm ơn bạn đã đăng ký tài khoản. Vui lòng nhấn vào liên kết sau để kích hoạt:\n\n" +
                duongDanKichHoat + "\n\n" +
                "Liên kết có hiệu lực trong 24 giờ.\n\n" +
                "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này."
        );

        mailSender.send(message);
    }

    @Async
    public void guiEmailDatLaiMatKhau(String emailNguoiNhan, String hoTen, String token) {
        String duongDanDatLai = urlFrontend + "/dat-lai-mat-khau?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(diaChiGui);
        message.setTo(emailNguoiNhan);
        message.setSubject("Đặt lại mật khẩu - Nền Tảng Học Tập Trực Tuyến");
        message.setText(
                "Chào " + hoTen + ",\n\n" +
                "Vui lòng nhấn vào liên kết sau để đặt lại mật khẩu:\n\n" +
                duongDanDatLai + "\n\n" +
                "Liên kết có hiệu lực trong 1 giờ.\n\n" +
                "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này."
        );

        mailSender.send(message);
    }

    /**
     * FIX (chống email enumeration ở /dang-ky): gửi cho CHỦ TÀI KHOẢN THẬT
     * khi có người khác thử đăng ký lại đúng email của họ, thay vì để API
     * trả lỗi "Email đã được sử dụng" (lộ thông tin email đã tồn tại cho
     * người gọi). Xem DangKyDichVuImpl.dangKy().
     */
    @Async
    public void guiEmailCanhBaoDangKyTrung(String emailNguoiNhan, String hoTen) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(diaChiGui);
        message.setTo(emailNguoiNhan);
        message.setSubject("Có người vừa thử đăng ký bằng email của bạn - Nền Tảng Học Tập Trực Tuyến");
        message.setText(
                "Chào " + hoTen + ",\n\n" +
                "Có người vừa thử tạo tài khoản mới bằng đúng địa chỉ email này, nhưng email của bạn " +
                "đã có tài khoản trên hệ thống nên yêu cầu đó không được thực hiện.\n\n" +
                "Nếu đó là bạn, vui lòng đăng nhập bằng tài khoản hiện có, hoặc dùng chức năng " +
                "\"Quên mật khẩu\" nếu không nhớ mật khẩu.\n\n" +
                "Nếu không phải bạn, bạn không cần làm gì thêm — tài khoản của bạn vẫn an toàn."
        );

        mailSender.send(message);
    }
}