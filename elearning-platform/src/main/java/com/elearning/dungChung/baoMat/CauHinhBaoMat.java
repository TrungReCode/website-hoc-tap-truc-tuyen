package com.elearning.dungChung.baoMat;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import java.util.List;

/**
 * Cấu hình bảo mật toàn hệ thống: vô hiệu hóa session (dùng JWT stateless),
 * khai báo endpoint công khai (đăng ký/đăng nhập/webhook IPN), và chèn
 * BoLocJWT vào trước UsernamePasswordAuthenticationFilter.
 *
 * Endpoint công khai cố ý liệt kê tường minh ở đây (không rải trong từng
 * Controller bằng @PreAuthorize) để có một nơi DUY NHẤT trả lời câu hỏi
 * "API nào không cần đăng nhập" — dễ kiểm tra bảo mật khi review code.
 */
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class CauHinhBaoMat {

    private final BoLocJWT boLocJWT;
    private final GioiHanTocDoFilter gioiHanTocDoFilter;

    // Các endpoint không yêu cầu xác thực JWT
    private static final String[] DUONG_DAN_CONG_KHAI = {
            "/api/xac-thuc/**", // đăng ký, đăng nhập, quên mật khẩu
            "/api/thanh-toan/vnpay-ipn", // webhook VNPay gọi vào, không có JWT
            "/api/thanh-toan/vnpay-return",
            "/api/khoa-hoc/cong-khai/**", // duyệt Chợ Khóa Học không cần đăng nhập
            "/api/lop-hoc/cong-khai/**", // duyệt danh sách lớp học đang hoạt động không cần đăng nhập
            "/api/chung-chi/xac-thuc/**", // ai cũng tra cứu được giấy xác nhận qua mã/QR, không cần đăng nhập
            "/api/danh-muc/**"
    };

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigurationSource)
            throws Exception {
        http
                // FIX (Vấn đề #2): accessToken/refreshToken nay nằm trong cookie
                // HttpOnly (trước đây trong header Authorization do frontend tự
                // gắn, không cần CSRF vì trình duyệt không tự đính header đó).
                // Vẫn TẮT Spring CSRF filter (không phải bật lại) vì đã chọn
                // SameSite=Lax cho cả 2 cookie ở CongCuCookie — với API JSON gọi
                // qua fetch/axios, SameSite=Lax đã đủ chặn cookie bị gửi kèm
                // request cross-site giả mạo, tương đương hiệu quả CSRF token
                // truyền thống mà không cần thêm round-trip lấy token. Xem
                // Javadoc của CongCuCookie phần lưu ý khi deploy domain tách biệt.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Chưa có/không còn cookie accessToken -> trả 401 kèm JSON (mặc định
                // Spring trả 403 body rỗng) để frontend nhận ra cần gọi
                // lam-moi-token và có thông báo lỗi đọc được.
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                            "{\"thongBao\": \"Chưa đăng nhập hoặc phiên đã hết hạn.\", \"maLoi\": \"CHUA_XAC_THUC\"}");
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(DUONG_DAN_CONG_KHAI).permitAll()
                        .anyRequest().authenticated())
                // NÂNG CẤP: GioiHanTocDoFilter chạy TRƯỚC boLocJWT — rate limit
                // phải áp dụng cho MỌI request kể cả chưa xác thực (vd. chặn
                // brute-force/DoS nhắm vào /dang-nhap trước khi tới tận BoLocJWT),
                // nên addFilterBefore(boLocJWT) thay vì addFilterAfter.
                .addFilterBefore(boLocJWT, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(gioiHanTocDoFilter, BoLocJWT.class);

        return http.build();
    }

    /**
     * CORS cho môi trường DEV (profile "dev", mặc định khi không set
     * SPRING_PROFILES_ACTIVE): cho phép mọi cổng trên localhost vì Vite có
     * thể chạy ở 5173/5174/... tùy máy. KHÔNG dùng bean này cho production —
     * origin dạng wildcard "localhost:*" sẽ vô nghĩa/không khớp domain thật.
     * Đặt tên bean tường minh ("corsConfigurationSource", khớp @Qualifier ở
     * filterChain) — nếu không, Spring inject theo tên tham số sẽ nhầm với
     * bean mvcHandlerMappingIntrospector của Spring MVC (bean này cũng
     * implement CorsConfigurationSource) khi build không giữ lại tên tham số.
     */
    @Bean("corsConfigurationSource")
    @Profile("dev")
    public CorsConfigurationSource corsConfigurationSourceDev() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                // Cho phép truy cập từ điện thoại/thiết bị khác cùng mạng LAN khi
                // test — CẦN THIẾT để quét mã QR trên giấy xác nhận hoạt động
                // được (điện thoại không thể mở "http://localhost:..." vì đó là
                // chính nó, không phải máy đang chạy backend). Xoá dòng dưới nếu
                // không test qua LAN, hoặc thay đúng dải mạng thật của máy.
                "http://192.168.*.*:*",
                "http://10.*.*.*:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * CORS cho môi trường PROD (profile "prod"): CHỈ cho phép đúng domain
     * frontend thật (biến môi trường FRONTEND_URL, xem application-prod.yml),
     * không có wildcard. Nhờ tách theo @Profile, việc quên siết CORS khi
     * deploy production giờ sẽ khiến ứng dụng không khởi động được đúng cấu
     * hình mong muốn thay vì âm thầm dùng cấu hình localhost — bắt buộc phải
     * set FRONTEND_URL khi chạy profile "prod".
     */
    @Bean("corsConfigurationSource")
    @Profile("prod")
    public CorsConfigurationSource corsConfigurationSourceProd(
            @Value("${app.frontend-url}") String frontendUrl) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            ChiTietNguoiDungDichVu chiTietNguoiDungDichVu, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(chiTietNguoiDungDichVu);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public FilterRegistrationBean<BoLocJWT> boLocJWTRegistration(BoLocJWT filter) {
        FilterRegistrationBean<BoLocJWT> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<GioiHanTocDoFilter> gioiHanTocDoRegistration(GioiHanTocDoFilter filter) {
        FilterRegistrationBean<GioiHanTocDoFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }
}