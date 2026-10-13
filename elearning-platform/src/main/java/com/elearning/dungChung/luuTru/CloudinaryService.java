package com.elearning.dungChung.luuTru;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Hàm upload các file tài liệu lớn (PDF, DOCX, ZIP...) giữ nguyên bản
     */
    @SuppressWarnings("unchecked")
    public String uploadTaiLieuLon(MultipartFile file) throws IOException {
        // Cấu hình các tham số upload
        Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", "raw", // QUAN TRỌNG: "raw" dùng cho file giữ nguyên bản để tải về
                "folder", "elearning-platform/tai-lieu", // Thư mục lưu trên Cloudinary
                "chunk_size", 20000000 // (Tuỳ chọn) Kích thước mỗi chunk: 20MB (20,000,000 bytes)
        );

        // Sử dụng uploadLarge thay vì upload thông thường
        // Thích hợp cho file từ vài chục đến hàng trăm MB
        Map<String, Object> uploadResult = cloudinary.uploader().uploadLarge(file.getInputStream(), options);

        // Lấy đường dẫn an toàn (HTTPS) để lưu vào Database
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Hàm upload Video lớn (hỗ trợ Chunked Upload 20MB/lần)
     * Trả về link video có thể stream trực tiếp trên trình duyệt
     */
    @SuppressWarnings("unchecked")
    public String uploadVideoLon(MultipartFile file) throws IOException {
        // Cấu hình các tham số upload cho VIDEO
        Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", "video", // QUAN TRỌNG NHẤT: Đổi từ "raw" sang "video"
                "folder", "elearning-platform/video", // Thư mục riêng cho gọn
                "chunk_size", 20000000 // Chia nhỏ 20MB mỗi gói
        );

        // Sử dụng uploadLarge để đẩy file 200MB lên an toàn
        Map<String, Object> uploadResult = cloudinary.uploader().uploadLarge(file.getInputStream(), options);

        // Lấy đường dẫn HTTPS trả về cho Database
        return uploadResult.get("secure_url").toString();
    }
}
