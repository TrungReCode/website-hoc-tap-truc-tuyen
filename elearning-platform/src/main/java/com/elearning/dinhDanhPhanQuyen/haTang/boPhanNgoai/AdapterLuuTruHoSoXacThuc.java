package com.elearning.dinhDanhPhanQuyen.haTang.boPhanNgoai;

import com.elearning.dungChung.ngoaiLe.NgoaiLeNghiepVu;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

/**
 * Adapter lưu trữ file RIÊNG TƯ cho module dinhDanhPhanQuyen (hiện dùng cho
 * minh chứng bằng cấp/CCCD khi xác thực chuyên môn giảng viên) — dữ liệu cá
 * nhân nhạy cảm, KHÔNG được để công khai qua r2.public-base-url như các
 * Adapter khác.
 *
 * FIX (tách bucket riêng): dùng r2.private-bucket — một bucket R2 THỨ HAI,
 * tách biệt hoàn toàn khỏi r2.bucket (bucket công khai dùng chung cho 4
 * Adapter còn lại). Bucket này KHÔNG BAO GIỜ bật Public Development URL hay
 * Custom Domain trên Cloudflare Dashboard — chỉ đọc được qua S3Client có
 * credentials của server (GetObjectRequest), không tồn tại URL công khai
 * nào trỏ vào nó dù vô tình hay cố ý.
 *
 * CỐ Ý KHÔNG dùng chung AdapterLuuTruHocLieu (choKhoaHoc.haTang.boPhanNgoai)
 * dù logic gần như giống hệt — nếu dinhDanhPhanQuyen import từ choKhoaHoc sẽ
 * vi phạm ranh giới module đã thiết lập (dinhDanhPhanQuyen là module nền
 * tảng, các module khác được phép phụ thuộc VÀO nó qua NguoiDungTraCuuDichVu,
 * không phải chiều ngược lại — xem javadoc NguoiDungTraCuuDichVu). Chấp nhận
 * trùng lặp ~15 dòng với AdapterLuuTruHocLieu để giữ đúng kiến trúc phân lớp.
 *
 * FIX: chuyển từ Firebase Storage (Admin SDK, bỏ qua Security Rules) sang
 * Cloudflare R2 — bản chất bảo mật KHÔNG đổi: file vẫn chỉ đọc được qua
 * đúng S3Client có credentials của server (GetObjectRequest), không có URL
 * công khai nào tồn tại cho đối tượng này. Việc kiểm tra quyền truy cập vẫn
 * do service gọi vào đây tự chịu trách nhiệm TRƯỚC — hàm taiFileRiengTu()
 * không tự kiểm tra quyền gì cả, y hệt trước đây.
 */
@Component
@RequiredArgsConstructor
public class AdapterLuuTruHoSoXacThuc {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterLuuTruHoSoXacThuc.class);

    private final S3Client r2Client;

    @Value("${r2.private-bucket}")
    private String bucket;

    /**
     * Trả về ĐƯỜNG DẪN LƯU TRỮ nội bộ (không phải URL công khai) — file này
     * không có URL công khai nào để lộ ra ngoài; chỉ tải lại được qua
     * taiFileRiengTu() bên dưới, gọi từ 1 endpoint có @PreAuthorize riêng.
     */
    public String taiLenFileRiengTu(MultipartFile file, String thuMuc) {
        try {
            String tenDuongDan = thuMuc + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(tenDuongDan)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));

            return tenDuongDan;
        } catch (Exception ex) {
            throw new NgoaiLeNghiepVu("Tải file lên R2 thất bại: " + ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE, "LOI_TAI_LEN_FILE_RIENG_TU");
        }
    }

    /**
     * FIX: xóa file riêng tư cũ khỏi R2 — gọi khi giảng viên GỬI LẠI hồ sơ
     * xác thực sau khi bị từ chối (XacThucGiangVienDichVuImpl.guiYeuCau ghi
     * đè duongDanLuuTru bằng file mới, bỏ quên file cũ). Tham số nhận ĐƯỜNG
     * DẪN LƯU TRỮ nội bộ (như taiLenFileRiengTu trả về), KHÔNG phải URL công
     * khai — vì file riêng tư không có URL công khai nào cả.
     */
    public void xoa(String duongDanLuuTru) {
        if (duongDanLuuTru == null || duongDanLuuTru.isBlank()) {
            return;
        }
        try {
            r2Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(duongDanLuuTru).build());
        } catch (Exception ex) {
            LOG.warn("Xóa file riêng tư cũ trên R2 thất bại, file có thể vẫn còn tồn tại: {}", duongDanLuuTru, ex);
        }
    }

    /**
     * CHỈ được gọi từ service đã tự kiểm tra quyền truy cập của người gọi
     * TRƯỚC (hàm này không tự kiểm tra quyền gì cả) — bucket R2 không bật
     * public access nên chỉ server (cầm credentials ở CauHinhR2) đọc được.
     */
    public byte[] taiFileRiengTu(String duongDanLuuTru) {
        try (ResponseInputStream<GetObjectResponse> doc = r2Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(duongDanLuuTru)
                        .build())) {
            return doc.readAllBytes();
        } catch (NoSuchKeyException ex) {
            throw new NgoaiLeNghiepVu("Không tìm thấy file trên storage", HttpStatus.NOT_FOUND,
                    "KHONG_TIM_THAY_FILE");
        } catch (Exception ex) {
            throw new NgoaiLeNghiepVu("Tải file từ R2 thất bại: " + ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE, "LOI_TAI_FILE_RIENG_TU");
        }
    }
}