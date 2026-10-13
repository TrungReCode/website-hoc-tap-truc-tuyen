package com.elearning.choKhoaHoc.haTang.boPhanNgoai;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.elearning.dungChung.ngoaiLe.NgoaiLeNghiepVu;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter duy nhất trong hệ thống được phép gọi ra dịch vụ lưu trữ tệp
 * bên ngoài (Cloudinary cho video, Cloudflare R2 cho tài liệu PDF/DOC).
 * Service trong ungDung.impl không bao giờ gọi thẳng SDK của Cloudinary
 * hay R2 — luôn đi qua Adapter này, để khi đổi nhà cung cấp lưu trữ
 * sau này chỉ cần sửa một file duy nhất.
 *
 * FIX: chuyển phần tài liệu PDF/DOC từ Firebase Storage sang Cloudflare R2
 * (xem CauHinhR2) — S3Client dùng chung được tiêm qua constructor, an toàn
 * khởi tạo eager (không đọc file/gọi mạng lúc khởi tạo) nên không cần cơ chế
 * khởi tạo lười như Firebase trước đây.
 */
@Component
@RequiredArgsConstructor
public class AdapterLuuTruHocLieu {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterLuuTruHocLieu.class);

    private final S3Client r2Client;

    @Value("${cloudinary.cloud-name}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.api-key}")
    private String cloudinaryApiKey;

    @Value("${cloudinary.api-secret}")
    private String cloudinaryApiSecret;

    @Value("${r2.bucket}")
    private String r2Bucket;

    @Value("${r2.public-base-url}")
    private String r2PublicBaseUrl;

    private Cloudinary cloudinary;

    @PostConstruct
    void khoiTaoCloudinary() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudinaryCloudName,
                "api_key", cloudinaryApiKey,
                "api_secret", cloudinaryApiSecret,
                "secure", true));
    }

    // Giới hạn cứng của gói Cloudinary FREE cho video — không có cách nào vượt
    // qua được kể cả bằng uploadLarge/chunked upload (đã xác nhận qua hỗ trợ
    // Cloudinary), phải nâng cấp gói trả phí mới tăng được giới hạn này.
    // Kiểm tra trước ở đây để báo lỗi rõ ràng ngay lập tức, thay vì để người
    // dùng chờ upload xong (có thể vài phút với mạng chậm) rồi mới nhận lỗi.
    private static final long GIOI_HAN_VIDEO_BYTES = 100L * 1024 * 1024; // 100 MB

    /**
     * Tải video bài giảng lên Cloudinary, trả về URL công khai + metadata kỹ thuật
     * trích xuất tự động.
     */
    public KetQuaTaiLenVideo taiLenVideo(MultipartFile file) {
        if (file.getSize() > GIOI_HAN_VIDEO_BYTES) {
            throw new NgoaiLeNghiepVu(
                    String.format("Video %.1fMB vượt quá giới hạn 100MB của gói lưu trữ hiện tại. "
                                    + "Vui lòng nén video (giảm độ phân giải/bitrate) rồi thử lại.",
                            file.getSize() / 1024.0 / 1024.0),
                    HttpStatus.PAYLOAD_TOO_LARGE, "VIDEO_VUOT_GIOI_HAN");
        }
        try {
            // SỬ DỤNG UPLOAD_LARGE VÀ THÊM CHUNK_SIZE ĐỂ XỬ LÝ VIDEO > 100MB
            //
            // "type": "authenticated" — KHÁC với mặc định "upload" (công khai
            // vĩnh viễn). Đây là điều kiện BẮT BUỘC để có thể sinh signed URL
            // có hạn dùng sau này (layDuongDanVideoCoChuKy) — tài nguyên loại
            // "upload" không thể "ký thêm hạn dùng" bằng bất kỳ cách nào, vì
            // bản thân URL gốc (không ký) vẫn luôn truy cập công khai được.
            // CHỈ áp dụng cho video upload MỚI kể từ đây — video cũ đã tồn
            // tại trước khi thêm dòng này vẫn ở dạng "upload" công khai,
            // không thể bảo vệ hồi tố nếu không tải lại (xem thêm chú thích
            // ở layDuongDanVideoCoChuKy).
            Map<?, ?> ketQua = cloudinary.uploader().uploadLarge(file.getInputStream(), ObjectUtils.asMap(
                    "resource_type", "video",
                    "type", "authenticated",
                    "folder", "elearning/video-bai-hoc",
                    "chunk_size", 20000000,
                    // Timeout tường minh cho từng request chunk — trước đây
                    // không set gì, phụ thuộc hoàn toàn vào giá trị mặc định
                    // của Cloudinary Java SDK (chưa từng được xác nhận có
                    // phù hợp với việc tải file lớn qua mạng chậm hay không).
                    // 120s đủ rộng cho 1 chunk 20MB qua kết nối chậm.
                    "timeout", 120000));

            String url = (String) ketQua.get("secure_url");
            String dinhDang = (String) ketQua.get("format");
            Integer thoiLuongGiay = ketQua.get("duration") != null
                    ? (int) Math.round(Double.parseDouble(ketQua.get("duration").toString()))
                    : null;
            Integer chieuRong = ketQua.get("width") != null ? ((Number) ketQua.get("width")).intValue() : null;
            Integer chieuCao = ketQua.get("height") != null ? ((Number) ketQua.get("height")).intValue() : null;

            return new KetQuaTaiLenVideo(url, dinhDang, thoiLuongGiay, chieuRong, chieuCao);
        } catch (IOException ex) {
            throw new NgoaiLeNghiepVu("Tải video lên Cloudinary thất bại: " + ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE, "LOI_TAI_LEN_VIDEO");
        }
    }

    // Khớp URL video Cloudinary do CHÍNH adapter này sinh ra (2 dạng: "upload"
    // công khai cũ, hoặc "authenticated" mới) để tách lấy public_id — dùng để
    // sinh lại signed URL mà KHÔNG cần thêm cột CSDL lưu public_id riêng.
    // Nhóm 1 = loại tài nguyên (upload|authenticated), nhóm 2 = public_id.
    private static final Pattern MAU_URL_VIDEO_CLOUDINARY = Pattern.compile(
            "^https://res\\.cloudinary\\.com/[^/]+/video/(upload|authenticated)(?:/s--[^/]+--)?/v\\d+/(.+)\\.[a-zA-Z0-9]+$");

    /**
     * Sinh URL xem video có CHỮ KÝ từ URL đã lưu trong CSDL — dùng thay cho
     * việc trả thẳng URL cố định mỗi khi học viên xem giáo trình.
     *
     * CHỈ có tác dụng với video upload SAU KHI đổi type="authenticated"
     * (xem taiLenVideo ở trên). Với video CŨ (type="upload", công khai từ
     * trước), URL gốc bản thân nó đã truy cập công khai được không cần ký —
     * hàm này trả nguyên văn URL cũ, KHÔNG cố parse/ký giả, vì có ký cũng
     * không tăng thêm bảo mật gì (tài nguyên gốc vẫn công khai song song).
     * Video cũ chỉ được bảo vệ thật khi giảng viên tải lại (re-upload).
     *
     * LƯU Ý TRIỂN KHAI: cloudinary.url()...signed(true) với type=
     * "authenticated" là API chuẩn, tài liệu hoá chính thức của Cloudinary,
     * đã đủ để chặn truy cập trực tiếp không qua chữ ký hợp lệ. Muốn tự
     * chọn thời hạn ngắn tuỳ ý (VD: hết hạn sau đúng 10 phút) cần thêm bước
     * bật "Token-based authentication" trong Cloudinary Console (Settings →
     * Security) và dùng thêm class AuthToken của SDK — chưa làm ở đây vì
     * cần xác nhận gói Cloudinary đang dùng có hỗ trợ tính năng đó không.
     */
    public String layDuongDanVideoCoChuKy(String duongDanDaLuu) {
        if (duongDanDaLuu == null || duongDanDaLuu.isBlank()) {
            return duongDanDaLuu;
        }
        if (cloudinaryCloudName == null || cloudinaryCloudName.isBlank()
                || cloudinaryApiSecret == null || cloudinaryApiSecret.isBlank()) {
            return duongDanDaLuu;
        }
        Matcher m = MAU_URL_VIDEO_CLOUDINARY.matcher(duongDanDaLuu);
        if (!m.matches() || !"authenticated".equals(m.group(1))) {
            // Video cũ (type=upload) hoặc URL lạ không khớp định dạng đã biết
            // (VD: link ngoài do sửa tay CSDL) — trả nguyên văn, an toàn hơn
            // là ném lỗi làm sập tính năng xem video.
            return duongDanDaLuu;
        }
        String publicId = m.group(2);
        try {
            return cloudinary.url()
                    .resourceType("video")
                    .type("authenticated")
                    .signed(true)
                    .generate(publicId);
        } catch (RuntimeException ex) {
            // URL ký là lớp bảo vệ bổ sung; không làm hỏng toàn bộ chi tiết
            // khóa học khi môi trường dev thiếu cấu hình Cloudinary.
            return duongDanDaLuu;
        }
    }

    /**
     * Tải tài liệu (PDF/DOC) lên Cloudflare R2, trả về URL tải xuống công khai.
     */
    public KetQuaTaiLenTaiLieu taiLenTaiLieu(MultipartFile file) {
        try {
            String tenDuongDan = "elearning/tai-lieu-bai-hoc/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(r2Bucket)
                            .key(tenDuongDan)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));

            String url = r2PublicBaseUrl + "/" + tenDuongDan;

            return new KetQuaTaiLenTaiLieu(url, layDuoiTep(file.getOriginalFilename()), file.getSize());
        } catch (Exception ex) {
            throw new NgoaiLeNghiepVu("Tải tài liệu lên R2 thất bại: " + ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE, "LOI_TAI_LEN_TAI_LIEU");
        }
    }

    /**
     * FIX: xóa tài liệu PDF/DOC khỏi R2 — gọi khi bản ghi BaiHoc tương ứng bị
     * xóa (xoaBaiHoc), để dung lượng lưu trữ thật sự giảm thay vì để lại file
     * mồ côi (vẫn tính phí lưu trữ dù không còn bản ghi DB nào tham chiếu).
     * CHỈ dùng cho URL do chính taiLenTaiLieu() ở trên tạo ra — không dùng
     * cho URL video (Cloudinary, không nằm trên R2).
     *
     * Cố ý KHÔNG ném lỗi nếu xóa thất bại (vd. mất mạng tới R2): dọn dẹp
     * storage là việc phụ, không được làm hỏng thao tác chính (xóa bản ghi
     * DB) — chỉ log lại để biết mà dọn thủ công sau.
     */
    public void xoaTaiLieu(String url) {
        if (url == null || url.isBlank() || !url.startsWith(r2PublicBaseUrl + "/")) {
            return;
        }
        try {
            String key = url.substring((r2PublicBaseUrl + "/").length());
            r2Client.deleteObject(DeleteObjectRequest.builder().bucket(r2Bucket).key(key).build());
        } catch (Exception ex) {
            LOG.warn("Xóa tài liệu trên R2 thất bại, file có thể vẫn còn tồn tại: {}", url, ex);
        }
    }

    private String layDuoiTep(String tenTep) {
        if (tenTep == null || !tenTep.contains(".")) {
            return null;
        }
        return tenTep.substring(tenTep.lastIndexOf('.') + 1).toLowerCase();
    }
}