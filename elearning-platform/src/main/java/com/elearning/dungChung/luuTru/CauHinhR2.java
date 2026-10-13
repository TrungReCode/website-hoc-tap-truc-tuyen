package com.elearning.dungChung.luuTru;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * FIX (chuyển lưu trữ Firebase Storage -> Cloudflare R2): Cloudflare R2
 * tương thích API S3 nên dùng thẳng S3Client của AWS SDK v2, chỉ khác
 * endpoint (trỏ về R2 thay vì AWS) và region đặt cố định "auto" theo đúng
 * tài liệu chính thức của Cloudflare.
 *
 * KHÁC VỚI FIREBASE: không cần khởi tạo lười (lazy) như trước — constructor
 * của S3Client chỉ giữ cấu hình (endpoint, credentials) trong bộ nhớ, KHÔNG
 * đọc file hay gọi mạng, nên an toàn khởi tạo eager ngay cả khi r2.* còn để
 * trống ở môi trường dev chưa cấu hình xong (giống Cloudinary đã làm trước
 * giờ — xem comment cũ trong AdapterLuuTruHocLieu). Lỗi (nếu có) chỉ xảy ra
 * khi THỰC SỰ gọi putObject/getObject, không phải lúc Spring khởi động.
 *
 * S3Client bean này được 5 Adapter dùng chung (học liệu, học liệu bổ sung,
 * đính kèm diễn đàn, giấy xác nhận, hồ sơ xác thực giảng viên) — mỗi Adapter
 * vẫn tự giữ logic build đường dẫn/URL riêng (đúng tinh thần duplicate có
 * chủ đích để giữ ranh giới module, như đã ghi trong
 * AdapterLuuTruHoSoXacThuc), chỉ phần client kết nối là dùng chung.
 */
@Configuration
public class CauHinhR2 {

    @Value("${r2.account-id}")
    private String accountId;

    @Value("${r2.access-key-id}")
    private String accessKeyId;

    @Value("${r2.secret-access-key}")
    private String secretAccessKey;

    @Bean
    public S3Client r2Client() {
        return S3Client.builder()
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                // R2 không có khái niệm vùng (region) như AWS thật — "auto" là
                // giá trị cố định theo đúng tài liệu Cloudflare, SDK vẫn cần
                // một Region hợp lệ về mặt cú pháp để ký request (SigV4).
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();
    }
}
