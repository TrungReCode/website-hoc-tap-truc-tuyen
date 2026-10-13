package com.elearning.dungChung.baoMat;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * FIX (Vấn đề #2): trước đây XacThucController trả accessToken/refreshToken
 * thẳng trong JSON body, frontend tự lưu vào localStorage rồi tự gắn header
 * "Authorization: Bearer ..." cho mỗi request (xem apiClient.js cũ). Rủi ro:
 * bất kỳ đoạn JavaScript nào chạy được trên trang (XSS) đều ĐỌC được token
 * từ localStorage. Class này chuyển 2 token sang cookie HttpOnly — JS không
 * đọc được giá trị cookie này dù có XSS, giảm hẳn thiệt hại nếu sau này lộ
 * lỗ hổng XSS ở đâu đó (bản thân đợt rà soát này không tìm thấy XSS nào,
 * nhưng "không tìm thấy hôm nay" không có nghĩa là "sẽ luôn an toàn").
 *
 * SameSite=Lax (không phải None):
 * - Đủ để chặn CSRF cho API JSON gọi qua fetch/axios (trình duyệt KHÔNG gửi
 *   cookie SameSite=Lax kèm request cross-site không phải điều hướng
 *   top-level dạng GET) — đây là lý do CauHinhBaoMat vẫn tắt Spring CSRF
 *   filter thay vì bật thêm cơ chế double-submit token.
 * - Vẫn gửi kèm bình thường giữa frontend (vd. localhost:5173, hoặc qua
 *   Vite dev proxy nên coi như cùng origin) và backend (localhost:8080) dù
 *   khác PORT, vì SameSite xét theo "site" (domain đăng ký + scheme), KHÔNG
 *   xét theo port.
 *
 * LƯU Ý KHI DEPLOY THẬT: nếu domain frontend và backend production KHÔNG
 * cùng site (vd. app.example.com và api-rieng.com — 2 domain đăng ký khác
 * nhau, không chỉ khác subdomain), đó là cross-site thật sự — SameSite=Lax
 * sẽ ÂM THẦM CHẶN LUÔN cookie hợp lệ. Khi đó phải đổi sang "None" (bắt buộc
 * đi kèm secure=true) và bổ sung cơ chế chống CSRF khác thay cho SameSite.
 */
@Component
public class CongCuCookie {

    public static final String TEN_COOKIE_ACCESS_TOKEN = "accessToken";
    public static final String TEN_COOKIE_REFRESH_TOKEN = "refreshToken";

    /** refreshToken chỉ thật sự cần thiết ở các endpoint /api/xac-thuc/**
     *  (lam-moi-token, dang-xuat) — không cần gửi kèm MỌI request như
     *  accessToken, nên giới hạn path để giảm bề mặt lộ. */
    private static final String PATH_REFRESH_TOKEN = "/api/xac-thuc";

    private final long thoiHanAccessTokenMs;
    private final long thoiHanRefreshTokenMs;
    private final boolean secure;

    public CongCuCookie(
            @Value("${app.jwt.access-token-expiration-ms}") long thoiHanAccessTokenMs,
            @Value("${app.jwt.refresh-token-expiration-ms}") long thoiHanRefreshTokenMs,
            @Value("${app.cookie.secure:false}") boolean secure) {
        this.thoiHanAccessTokenMs = thoiHanAccessTokenMs;
        this.thoiHanRefreshTokenMs = thoiHanRefreshTokenMs;
        this.secure = secure;
    }

    /** Set cả 2 cookie sau khi đăng nhập thành công hoặc làm mới token. */
    public void datCookieDangNhap(HttpServletResponse response, String accessToken, String refreshToken) {
        // Max-Age của cookie accessToken KHÔNG được bằng thời hạn JWT: khi cookie
        // hết hạn trình duyệt tự xoá nó, request sau đó không mang token nào nên
        // BoLocJWT không có gì để báo ExpiredJwtException -> không có 401
        // TOKEN_HET_HAN -> frontend không biết cần gọi lam-moi-token. Cho cookie
        // sống bằng thời hạn refresh token; hiệu lực thật của access token vẫn do
        // claim exp trong JWT quyết định và luôn được backend kiểm tra.
        themCookie(response, taoCookie(TEN_COOKIE_ACCESS_TOKEN, accessToken, "/",
                Duration.ofMillis(Math.max(thoiHanAccessTokenMs, thoiHanRefreshTokenMs))));
        themCookie(response, taoCookie(TEN_COOKIE_REFRESH_TOKEN, refreshToken, PATH_REFRESH_TOKEN,
                Duration.ofMillis(thoiHanRefreshTokenMs)));
    }

    /** Xóa cả 2 cookie khi đăng xuất (hoặc khi refresh token không còn hợp lệ). */
    public void xoaCookieDangNhap(HttpServletResponse response) {
        themCookie(response, taoCookie(TEN_COOKIE_ACCESS_TOKEN, "", "/", Duration.ZERO));
        themCookie(response, taoCookie(TEN_COOKIE_REFRESH_TOKEN, "", PATH_REFRESH_TOKEN, Duration.ZERO));
    }

    private void themCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private ResponseCookie taoCookie(String ten, String giaTri, String path, Duration thoiHan) {
        return ResponseCookie.from(ten, giaTri)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(path)
                .maxAge(thoiHan)
                .build();
    }
}