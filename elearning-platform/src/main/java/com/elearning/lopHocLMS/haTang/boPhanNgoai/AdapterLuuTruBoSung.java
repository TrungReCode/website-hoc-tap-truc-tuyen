package com.elearning.lopHocLMS.haTang.boPhanNgoai;

import com.elearning.dungChung.ngoaiLe.NgoaiLeNghiepVu;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

/**
 * Theo đúng tài liệu kiến trúc: học liệu bổ sung của Giảng viên (video/PDF/
 * DOC/ZIP tự tải lên, không qua kiểm duyệt) chỉ dùng kho lưu trữ tài liệu
 * tĩnh (Cloudflare R2) — khác với học liệu gốc của choKhoaHoc dùng cả
 * Cloudinary cho video. Lý do hợp lý: học liệu bổ sung thường là tệp phụ
 * trợ (slide, tài liệu đọc thêm), không cần các tính năng streaming/
 * transcode chuyên biệt mà Cloudinary cung cấp cho video bài giảng chính.
 *
 * FIX: chuyển từ Firebase Storage sang R2 (xem CauHinhR2).
 */
@Component
@RequiredArgsConstructor
public class AdapterLuuTruBoSung {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterLuuTruBoSung.class);

    private final S3Client r2Client;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.public-base-url}")
    private String publicBaseUrl;

    public KetQuaTaiLenBoSung taiLen(MultipartFile file) {
        try {
            String tenDuongDan = "elearning/hoc-lieu-bo-sung/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(tenDuongDan)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));

            String url = publicBaseUrl + "/" + tenDuongDan;

            return new KetQuaTaiLenBoSung(url, layDuoiTep(file.getOriginalFilename()), file.getSize());
        } catch (Exception ex) {
            throw new NgoaiLeNghiepVu("Tải học liệu bổ sung lên R2 thất bại: " + ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE, "LOI_TAI_LEN_BO_SUNG");
        }
    }

    /**
     * FIX: xóa học liệu bổ sung khỏi R2 — gọi khi bản ghi BaiHocBoSung tương
     * ứng bị xóa (xoaBaiBoSung). CHỈ gọi hàm này với URL do chính taiLen() ở
     * trên tạo ra — KHÔNG gọi với loai = LINK (URL do giảng viên tự nhập,
     * không phải object trên R2) hay QUIZ (không có file). Dù vậy hàm vẫn tự
     * kiểm tra lại qua tiền tố publicBaseUrl trước khi xóa, để nếu nơi gọi
     * lỡ quên kiểm tra loai thì cũng không xóa nhầm URL không phải của mình.
     */
    public void xoa(String url) {
        if (url == null || url.isBlank() || !url.startsWith(publicBaseUrl + "/")) {
            return;
        }
        try {
            String key = url.substring((publicBaseUrl + "/").length());
            r2Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (Exception ex) {
            LOG.warn("Xóa học liệu bổ sung trên R2 thất bại, file có thể vẫn còn tồn tại: {}", url, ex);
        }
    }

    private String layDuoiTep(String tenTep) {
        if (tenTep == null || !tenTep.contains(".")) {
            return null;
        }
        return tenTep.substring(tenTep.lastIndexOf('.') + 1).toLowerCase();
    }
}