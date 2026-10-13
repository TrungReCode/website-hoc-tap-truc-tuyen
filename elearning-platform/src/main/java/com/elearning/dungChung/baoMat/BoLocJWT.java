package com.elearning.dungChung.baoMat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;

import java.io.IOException;

/**
 * Chạy một lần cho mỗi request, đọc accessToken, xác thực qua DichVuJWT, và
 * nếu hợp lệ thì set SecurityContext để các Controller phía sau biết "ai
 * đang gọi request này".
 *
 * FIX (Vấn đề #2): trước đây đọc token từ header "Authorization: Bearer ..."
 * do frontend tự gắn từ token lưu ở localStorage. Giờ token nằm trong cookie
 * HttpOnly "accessToken" (do CongCuCookie set ở XacThucController) — trình
 * duyệt tự động đính kèm cookie này vào mọi request, frontend không còn
 * cầm/đọc/gắn token thủ công nữa. Xem CongCuCookie để biết vì sao chuyển
 * sang cookie.
 */
@Component
@RequiredArgsConstructor
public class BoLocJWT extends OncePerRequestFilter {

    private final DichVuJWT dichVuJWT;
    private final ChiTietNguoiDungDichVu chiTietNguoiDungDichVu;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
            
        // 1. Cho phép các request OPTIONS đi qua luôn
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Bỏ qua các API công khai (như xác thực, kích hoạt)
        if (request.getServletPath().startsWith("/api/xac-thuc/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = layAccessTokenTuCookie(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String email;

        // 3. THÊM TRY-CATCH ĐỂ BẮT LỖI TOKEN HẾT HẠN HOẶC KHÔNG HỢP LỆ
        try {
            email = dichVuJWT.layEmailTuToken(token);

            // Token chức năng (kích hoạt / đặt lại mật khẩu) mang claim "muc_dich"
            // KHÔNG được chấp nhận làm access token — chặn ở đây để một token gửi
            // qua email không thể bị dùng để mạo danh đăng nhập trong lúc còn hạn.
            if (dichVuJWT.laTokenChucNang(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails nguoiDung = chiTietNguoiDungDichVu.loadUserByUsername(email);

                if (dichVuJWT.tokenHopLe(token, nguoiDung)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            nguoiDung, null, nguoiDung.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException e) {
            // Trả về JSON lỗi 401 để Frontend biết đường gọi API Refresh Token
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"thongBao\": \"Access Token đã hết hạn, vui lòng làm mới.\", \"maLoi\": \"TOKEN_HET_HAN\"}");
            return; // DỪNG REQUEST LẠI NGAY LẬP TỨC
            
        } catch (Exception e) {
            // Bắt các lỗi JWT khác (sai chữ ký, bị giả mạo...)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"thongBao\": \"Token không hợp lệ.\", \"maLoi\": \"TOKEN_KHONG_HOP_LE\"}");
            return; // DỪNG REQUEST LẠI NGAY LẬP TỨC
        }

        // Nếu token hợp lệ thì mới cho đi tiếp
        filterChain.doFilter(request, response);
    }

    private String layAccessTokenTuCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (CongCuCookie.TEN_COOKIE_ACCESS_TOKEN.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}