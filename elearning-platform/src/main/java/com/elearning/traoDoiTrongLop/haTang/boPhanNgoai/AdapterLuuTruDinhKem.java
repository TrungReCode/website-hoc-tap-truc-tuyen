package com.elearning.traoDoiTrongLop.haTang.boPhanNgoai;

import com.elearning.dungChung.ngoaiLe.NgoaiLeNghiepVu;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

/**
 * Lưu trữ file đính kèm bài đăng diễn đàn (UC-09/UC-10 — "đính kèm file
 * thực hành"). Cùng chọn kho lưu trữ tài liệu tĩnh như AdapterLuuTruBoSung
 * của lopHocLMS: đây là tài liệu phụ trợ (bài tập, tài liệu tham khảo),
 * không cần streaming/transcode như Cloudinary dành cho video bài giảng gốc.
 *
 * FIX: chuyển từ Firebase Storage sang Cloudflare R2 (xem CauHinhR2).
 */
@Component
@RequiredArgsConstructor
public class AdapterLuuTruDinhKem {

    private final S3Client r2Client;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.public-base-url}")
    private String publicBaseUrl;

    public KetQuaTaiLenDinhKem taiLen(MultipartFile file) {
        try {
            String tenDuongDan = "elearning/dinh-kem-dien-dan/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(tenDuongDan)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));

            String url = publicBaseUrl + "/" + tenDuongDan;

            return new KetQuaTaiLenDinhKem(url, layDuoiTep(file.getOriginalFilename()), file.getSize());
        } catch (Exception ex) {
            throw new NgoaiLeNghiepVu("Tải file đính kèm lên R2 thất bại: " + ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE, "LOI_TAI_LEN_DINH_KEM");
        }
    }

    private String layDuoiTep(String tenTep) {
        if (tenTep == null || !tenTep.contains(".")) {
            return null;
        }
        return tenTep.substring(tenTep.lastIndexOf('.') + 1).toLowerCase();
    }
}