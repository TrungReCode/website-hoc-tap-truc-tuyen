package com.elearning.chungChi.haTang;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tạo file PDF giấy xác nhận hoàn thành khóa học / lớp học bằng iText 8.
 * Không phụ thuộc Cloudinary — caller nhận byte[] rồi tự quyết định upload.
 */
@Component
public class TaoFilePdf {

    private static final DeviceRgb MAU_XANH_NAVY  = new DeviceRgb(27,  42,  74);
    private static final DeviceRgb MAU_XANH_TEAL  = new DeviceRgb(14, 124, 123);
    private static final DeviceRgb MAU_VANG        = new DeviceRgb(217, 164,  65);
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * @param tenHocVien     Họ tên học viên
     * @param tieuDeTaiNguyen  Tên khóa học / "Lớp [maLop] — Khóa học [tieuDeKhoaHoc]"
     * @param loai           "CA_NHAN" | "LOP_HOC"
     * @param tenGiangVien   null nếu loai = CA_NHAN
     * @param phanTramHoanThanh  0-100
     * @param diemTrungBinh  null nếu không có quiz
     * @param ngayCap        thời điểm cấp
     * @param maXacThuc      in lên giấy + QR
     * @param qrCodeBytes    ảnh QR đã render (PNG bytes), null nếu chưa có
     */
    public byte[] taoGiayXacNhan(
            String tenHocVien, String tieuDeTaiNguyen, String loai,
            String tenGiangVien, BigDecimal phanTramHoanThanh, BigDecimal diemTrungBinh,
            LocalDateTime ngayCap, String maXacThuc, byte[] qrCodeBytes) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdf, PageSize.A4.rotate()); // ngang — phong cách chứng chỉ
        doc.setMargins(36, 50, 36, 50);

        // FIX: Helvetica là font chuẩn PDF (Base-14), chỉ hỗ trợ bảng mã
        // WinAnsi/StandardEncoding — KHÔNG có dấu tiếng Việt (ơ, ư, ệ, ...),
        // khiến chữ có dấu bị vỡ/hiển thị sai trên file PDF sinh ra. Phải
        // dùng font TrueType hỗ trợ Unicode đầy đủ (Noto Sans, đóng gói sẵn
        // trong resources/fonts) và nhúng thẳng vào PDF (IDENTITY_H + embed
        // = true) để không phụ thuộc font cài sẵn trên máy người mở file.
        //
        // Đọc bằng byte[] qua classpath (không dùng đường dẫn file trực
        // tiếp) — bắt buộc để hoạt động đúng cả khi chạy từ .jar đã đóng
        // gói, vì lúc đó file font nằm bên trong jar, không còn là 1 file
        // thật trên đĩa để trỏ đường dẫn tới được.
        PdfFont fontThường = PdfFontFactory.createFont(
                docTepFontTuClasspath("NotoSans-Regular.ttf"), PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        PdfFont fontDam = PdfFontFactory.createFont(
                docTepFontTuClasspath("NotoSans-Bold.ttf"), PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);

        // ── Tiêu đề hệ thống ──────────────────────────────────────────────
        doc.add(new Paragraph("WEBSITE HỌC TẬP TRỰC TUYẾN")
                .setFont(fontDam).setFontSize(11)
                .setFontColor(MAU_XANH_TEAL)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

        String tieuDeGiay = loai.equals("LOP_HOC")
                ? "GIẤY XÁC NHẬN HOÀN THÀNH LỚP HỌC"
                : "GIẤY XÁC NHẬN HOÀN THÀNH KHÓA HỌC";

        doc.add(new Paragraph(tieuDeGiay)
                .setFont(fontDam).setFontSize(20)
                .setFontColor(MAU_XANH_NAVY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(6));

        // Đường kẻ vàng trang trí
        doc.add(new Table(1).useAllAvailableWidth()
                .addCell(new Cell().setHeight(3)
                        .setBackgroundColor(MAU_VANG)
                        .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER))
                .setMarginBottom(16));

        // ── Nội dung xác nhận ────────────────────────────────────────────
        doc.add(new Paragraph("Xác nhận học viên")
                .setFont(fontThường).setFontSize(12)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        doc.add(new Paragraph(tenHocVien)
                .setFont(fontDam).setFontSize(22)
                .setFontColor(MAU_XANH_NAVY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        String moTaHoanThanh = loai.equals("LOP_HOC")
                ? "đã hoàn thành lộ trình lớp học"
                : "đã hoàn thành khóa học";
        doc.add(new Paragraph(moTaHoanThanh)
                .setFont(fontThường).setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(6));

        doc.add(new Paragraph(tieuDeTaiNguyen)
                .setFont(fontDam).setFontSize(16)
                .setFontColor(MAU_XANH_TEAL)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(16));

        // ── Bảng chi tiết ────────────────────────────────────────────────
        Table bang = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(16);

        bang.addCell(chiTietCell("Tỷ lệ hoàn thành",
                phanTramHoanThanh.setScale(0, java.math.RoundingMode.HALF_UP) + "%",
                fontThường, fontDam));

        if (diemTrungBinh != null) {
            bang.addCell(chiTietCell("Điểm trung bình bài kiểm tra",
                    diemTrungBinh.setScale(1, java.math.RoundingMode.HALF_UP) + "/100",
                    fontThường, fontDam));
        } else {
            bang.addCell(chiTietCell("Điểm trung bình", "—", fontThường, fontDam));
        }

        bang.addCell(chiTietCell("Ngày hoàn thành",
                ngayCap.format(DINH_DANG_NGAY), fontThường, fontDam));
        doc.add(bang);

        if (tenGiangVien != null) {
            doc.add(new Paragraph("Giảng viên phụ trách: " + tenGiangVien)
                    .setFont(fontThường).setFontSize(11)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(8));
        }

        // ── Mã xác thực + QR ─────────────────────────────────────────────
        Table cuoi = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth();

        Cell cellMaXacThuc = new Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .add(new Paragraph("Mã xác thực")
                        .setFont(fontThường).setFontSize(9).setFontColor(ColorConstants.GRAY))
                .add(new Paragraph(maXacThuc)
                        .setFont(fontDam).setFontSize(13).setFontColor(MAU_XANH_NAVY))
                .add(new Paragraph("Quét mã QR hoặc tra cứu tại: /xac-thuc-giay/" + maXacThuc)
                        .setFont(fontThường).setFontSize(8).setFontColor(ColorConstants.GRAY));
        cuoi.addCell(cellMaXacThuc);

        if (qrCodeBytes != null) {
            Image qr = new Image(ImageDataFactory.create(qrCodeBytes))
                    .setWidth(70).setHeight(70)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT);
            cuoi.addCell(new Cell()
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                    .add(qr));
        } else {
            cuoi.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        }
        doc.add(cuoi);

        // Đường kẻ đáy
        doc.add(new Table(1).useAllAvailableWidth()
                .addCell(new Cell().setHeight(2)
                        .setBackgroundColor(MAU_XANH_NAVY)
                        .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER))
                .setMarginTop(8));

        doc.close();
        return baos.toByteArray();
    }

    /**
     * Đọc file font trong src/main/resources/fonts/ ra byte[] qua classpath
     * (KHÔNG dùng File/đường dẫn đĩa trực tiếp) — bắt buộc để hoạt động
     * đúng cả khi ứng dụng chạy từ .jar đã đóng gói (Spring Boot fat jar),
     * lúc đó thư mục resources không còn tồn tại như thư mục thật trên đĩa.
     */
    private byte[] docTepFontTuClasspath(String tenTep) {
        try {
            return StreamUtils.copyToByteArray(new ClassPathResource("fonts/" + tenTep).getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Không đọc được font " + tenTep + " — kiểm tra file có nằm đúng trong "
                            + "src/main/resources/fonts/ không", e);
        }
    }

    private Cell chiTietCell(String nhanHan, String giaTri, PdfFont fontThuong, PdfFont fontDam) {
        return new Cell()
                .setBorder(new SolidBorder(new DeviceRgb(220, 225, 232), 0.5f))
                .setBackgroundColor(new DeviceRgb(244, 246, 248))
                .setPadding(10)
                .add(new Paragraph(nhanHan)
                        .setFont(fontThuong).setFontSize(9).setFontColor(ColorConstants.GRAY))
                .add(new Paragraph(giaTri)
                        .setFont(fontDam).setFontSize(14)
                        .setFontColor(new DeviceRgb(31, 36, 48)));
    }
}
