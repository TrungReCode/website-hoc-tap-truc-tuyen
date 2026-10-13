package com.elearning.dungChung.baoMat;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NÂNG CẤP: rate limit trước đây chỉ có ở /dang-nhap (đếm tay qua Redis —
 * xem XacThucDichVuImpl). Filter này áp dụng rate limit cho TOÀN BỘ API theo
 * địa chỉ IP, bằng thuật toán token bucket (Bucket4j) — khác thuật toán
 * fixed-window counter đang dùng cho đăng nhập: token bucket cho phép burst
 * ngắn (dùng dồn token tích lũy) thay vì chặn cứng ngay khi chạm ngưỡng
 * trong 1 khung thời gian cố định, phù hợp hơn cho lưu lượng API thông
 * thường (người dùng bấm nhiều nút liên tiếp trong vài giây là bình thường).
 *
 * Mỗi IP: 1 bucket riêng, sức chứa {@value #SUC_CHUA} request, nạp lại
 * {@value #SUC_CHUA} token mỗi phút (trung bình ~{@value #SUC_CHUA} request/
 * phút, cho phép burst tới {@value #SUC_CHUA} request liền nếu bucket đang
 * đầy). Giá trị này là tạm, NÊN đo thực tế trước khi đưa vào production.
 *
 * GIỚI HẠN ĐÃ BIẾT: bucket lưu in-memory (ConcurrentHashMap), đúng cho 1
 * instance. Nếu scale ngang nhiều instance backend, mỗi instance đếm riêng —
 * cùng 1 IP có thể vượt ngưỡng thật nếu load balancer rải request qua nhiều
 * instance. Muốn đúng khi scale ngang cần đổi sang bucket4j-redis (đã có sẵn
 * RedisTemplate dùng chung trong dự án) để các instance chia sẻ cùng trạng
 * thái bucket — chưa làm ở bản này vì dự án hiện chạy 1 instance.
 *
 * Map cũng không có cơ chế tự dọn IP không hoạt động nữa — với traffic nhỏ
 * của dự án hiện tại không đáng lo, nhưng nếu traffic lớn hoặc chạy lâu dài
 * nên thêm TTL/eviction (vd. Caffeine cache thay cho ConcurrentHashMap).
 */
@Component
public class GioiHanTocDoFilter extends OncePerRequestFilter {

    private static final int SUC_CHUA = 120;
    private static final Duration CHU_KY_NAP_LAI = Duration.ofMinutes(1);

    private final Map<String, Bucket> bucketTheoIp = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // CORS preflight (OPTIONS) do trình duyệt tự gửi, không phải hành
        // động thật của người dùng — tính vào quota sẽ làm hao quota thật
        // một cách vô ích ở các request cross-origin cần preflight.
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String ip = layDiaChiIp(request);
        Bucket bucket = bucketTheoIp.computeIfAbsent(ip, k -> taoBucketMoi());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429); // 429 Too Many Requests
        response.setHeader("Retry-After", String.valueOf(CHU_KY_NAP_LAI.toSeconds()));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"thongBao\": \"Quá nhiều request, vui lòng thử lại sau.\", \"maLoi\": \"VUOT_GIOI_HAN_TOC_DO\"}");
    }

    private Bucket taoBucketMoi() {
        return Bucket.builder()
                .addLimit(gioiHan -> gioiHan.capacity(SUC_CHUA).refillGreedy(SUC_CHUA, CHU_KY_NAP_LAI))
                .build();
    }

    /**
     * Ưu tiên header X-Forwarded-For (đứng sau reverse proxy/load balancer
     * thật thì request.getRemoteAddr() chỉ trả về IP của proxy, không phải
     * IP người dùng thật) — lấy phần tử ĐẦU TIÊN trong danh sách vì đó là
     * client gốc, các phần tử sau là từng proxy trung gian đã đi qua.
     */
    private String layDiaChiIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
