package com.elearning.chungChi.haTang;

import com.elearning.dungChung.ngoaiLe.NgoaiLeNghiepVu;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

/**
 * FIX: chuyển từ Firebase Storage sang Cloudflare R2 (xem CauHinhR2) — cùng
 * lựa chọn hạ tầng với AdapterLuuTruDinhKem (traoDoiTrongLop), AdapterLuuTruBoSung
 * (lopHocLMS): tài liệu tĩnh, không cần streaming/transcode như Cloudinary
 * dành riêng cho video bài giảng gốc.
 *
 * Khác AdapterLuuTruDinhKem ở chỗ nhận thẳng byte[] thay vì MultipartFile,
 * vì PDF ở đây do server tự render (iText), không phải file người dùng tải
 * lên trực tiếp.
 */
@Component
@RequiredArgsConstructor
public class AdapterLuuTruGiayXacNhan {

    private final S3Client r2Client;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.public-base-url}")
    private String publicBaseUrl;

    /**
     * @param duLieu       nội dung file (bytes)
     * @param tenTepGoi    tên gợi ý, chỉ dùng để đặt phần đuôi/tiền tố cho dễ nhận diện trên R2
     * @param loaiNoiDung  vd "application/pdf", "image/png"
     * @param thuMucCon    vd "giay-xac-nhan-ca-nhan", "giay-xac-nhan-lop-hoc"
     */
    public String taiLen(byte[] duLieu, String tenTepGoi, String loaiNoiDung, String thuMucCon) {
        try {
            String tenDuongDan = "elearning/" + thuMucCon + "/" + UUID.randomUUID() + "_" + tenTepGoi;

            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(tenDuongDan)
                            .contentType(loaiNoiDung)
                            .build(),
                    RequestBody.fromBytes(duLieu));

            return publicBaseUrl + "/" + tenDuongDan;
        } catch (Exception ex) {
            throw new NgoaiLeNghiepVu("Tải giấy xác nhận lên R2 thất bại: " + ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE, "LOI_TAI_LEN_GIAY_XAC_NHAN");
        }
    }
}