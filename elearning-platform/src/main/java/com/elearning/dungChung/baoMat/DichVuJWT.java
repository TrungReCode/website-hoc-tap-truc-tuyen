package com.elearning.dungChung.baoMat;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Tạo và xác thực JWT (Access Token). Refresh Token được lưu ở DB
 * (bảng refresh_tokens) nên không cần JWT có thời hạn dài — chỉ Access
 * Token mới cần ký bằng thư viện jjwt.
 *
 * Đây là @Component dùng chung toàn hệ thống, đặt ở dungChung vì mọi
 * module (không chỉ IAM) đều cần BoLocJWT để xác thực request.
 */
@Component
public class DichVuJWT {

    private final SecretKey khoaBiMat;
    private final long thoiHanAccessToken;

    public DichVuJWT(
            @Value("${app.jwt.secret}") String chuoiKhoaBiMat,
            @Value("${app.jwt.access-token-expiration-ms}") long thoiHanAccessToken) {
        // Secret trong application.yml phải dài tối thiểu 32 ký tự (256 bit)
        // để HMAC-SHA256 hợp lệ. Dùng trực tiếp bytes, không cần encode lại.
        this.khoaBiMat = Keys.hmacShaKeyFor(chuoiKhoaBiMat.getBytes());
        this.thoiHanAccessToken = thoiHanAccessToken;
    }

    public String taoAccessToken(UserDetails nguoiDung) {
        Date baygio = new Date();
        Date hetHan = new Date(baygio.getTime() + thoiHanAccessToken);
        return Jwts.builder()
                .subject(nguoiDung.getUsername())
                .issuedAt(baygio)
                .expiration(hetHan)
                .signWith(khoaBiMat)
                .compact();
    }

    // Thêm hàm này vào DichVuJWT
    public String taoTokenKichHoat(String email) {
        return taoTokenChucNang(email, "KICH_HOAT", 900_000); // 15 phút
    }

    /**
     * Token đặt lại mật khẩu — có claim "muc_dich" riêng để BoLocJWT từ
     * chối dùng nó như access token thông thường (khác activation token
     * cũ chưa có claim này, chấp nhận rủi ro tồn đọng đó vì phạm vi ticket
     * hiện tại chỉ yêu cầu hoàn thiện luồng quên mật khẩu).
     */
    public String taoTokenDatLaiMatKhau(String email) {
        return taoTokenChucNang(email, "DAT_LAI_MAT_KHAU", 900_000); // 15 phút
    }

    private String taoTokenChucNang(String email, String mucDich, long thoiHanMs) {
        Date baygio = new Date();
        Date hetHan = new Date(baygio.getTime() + thoiHanMs);
        return Jwts.builder()
                .subject(email)
                .claim("muc_dich", mucDich)
                .issuedAt(baygio)
                .expiration(hetHan)
                .signWith(khoaBiMat)
                .compact();
    }

    /**
     * Xác thực token có "muc_dich" khớp yêu cầu (KICH_HOAT hoặc
     * DAT_LAI_MAT_KHAU) và chưa hết hạn. Trả về email nếu hợp lệ.
     * Dùng riêng cho các endpoint công khai /api/xac-thuc/**, không phải
     * cho luồng xác thực access token thông thường.
     */
    public String xacThucTokenChucNang(String token, String mucDichMongMuon) {
        String mucDich = layClaim(token, c -> c.get("muc_dich", String.class));
        if (!mucDichMongMuon.equals(mucDich)) {
            throw new io.jsonwebtoken.JwtException("Token không đúng mục đích sử dụng");
        }
        return layEmailTuToken(token);
    }

    /** true nếu token mang claim "muc_dich" (tức là token chức năng, KHÔNG phải access token thường). */
    public boolean laTokenChucNang(String token) {
        try {
            return layClaim(token, c -> c.get("muc_dich", String.class)) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public String layEmailTuToken(String token) {
        return layClaim(token, Claims::getSubject);
    }

    /**
     * Token hợp lệ khi: đúng chủ sở hữu, chưa hết hạn, VÀ tài khoản hiện tại
     * (load mới từ DB ở BoLocJWT cho mỗi request, không cache) vẫn đang ở
     * trạng thái HOAT_DONG. Nhờ vậy khi Quản trị viên khóa một tài khoản,
     * mọi access token đã cấp trước đó — dù chưa hết hạn theo thời gian —
     * đều bị từ chối ngay từ request kế tiếp, không phải chờ token tự hết
     * hạn (khắc phục UC-13: "kết thúc tất cả các phiên làm việc hiện có").
     */
    public boolean tokenHopLe(String token, UserDetails nguoiDung) {
        String email = layEmailTuToken(token);
        return email.equals(nguoiDung.getUsername())
                && !tokenHetHan(token)
                && nguoiDung.isEnabled()
                && nguoiDung.isAccountNonLocked();
    }

    private boolean tokenHetHan(String token) {
        return layClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T layClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(khoaBiMat)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}