package com.elearning.dungChung.nhatKy.impl;

import com.elearning.dungChung.nhatKy.NhatKyHeThongDichVu;
import com.elearning.dungChung.nhatKy.PhanHoiNhatKy;
import com.elearning.dungChung.nhatKy.PhanHoiNhatKy.MucNhatKy;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Đọc lại file audit.log do GhiNhatKyHanhDong ghi ra (xem logback-spring.xml).
 *
 * Giới hạn tối đa 1000 dòng/lần đọc để tránh QTV vô tình yêu cầu load file
 * quá lớn làm nghẽn heap — audit log có thể tồn tại tới 90 ngày (maxHistory
 * trong logback-spring.xml) nên không giới hạn dễ bị lạm dụng.
 *
 * Đọc theo kiểu "cửa sổ trượt" (Deque có kích thước cố định) thay vì load
 * toàn bộ file vào List rồi mới lấy N dòng cuối — bộ nhớ dùng chỉ tỉ lệ với
 * soDong yêu cầu, không tỉ lệ với kích thước file, dù file đã ghi hàng vạn
 * dòng trong ngày.
 */
@Service
public class NhatKyHeThongDichVuImpl implements NhatKyHeThongDichVu {

    private static final Logger log = LoggerFactory.getLogger(NhatKyHeThongDichVuImpl.class);
    private static final int SO_DONG_TOI_DA = 1000;
    private static final DateTimeFormatter DINH_DANG_TEN_FILE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Cùng nguồn property với logback-spring.xml (springProperty "thuMucLog")
    // — đảm bảo bên ghi và bên đọc luôn thống nhất 1 thư mục vật lý, bất kể
    // JVM khởi động từ đâu (IntelliJ, terminal, jar đóng gói...).
    @Value("${he-thong.thu-muc-log:./logs}")
    private String thuMucLogCauHinh;

    // Khớp định dạng ghi trong GhiNhatKyHanhDong:
    // 2026-08-03T14:18:22.256+07:00 actor=nguoi_dung#5 action=KHOA_TAI_KHOAN target=nguoi_dung#42 detail=...
    private static final Pattern MAU_DONG_LOG = Pattern.compile(
            "^(?<thoiGian>\\S+)\\s+actor=(?<actor>\\S+)\\s+action=(?<action>\\S+)\\s+target=(?<target>\\S+)\\s+detail=(?<detail>.*)$");

    @Override
    public PhanHoiNhatKy xemNhatKy(LocalDate ngay, int soDong, String tuKhoa) {
        LocalDate ngayThucTe = ngay != null ? ngay : LocalDate.now();
        int soDongThucTe = Math.min(Math.max(soDong, 1), SO_DONG_TOI_DA);
        String tuKhoaChuanHoa = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim().toLowerCase();

        if (ngayThucTe.isAfter(LocalDate.now())) {
            throw new NgoaiLeDuLieuKhongHopLe("Không thể xem nhật ký của một ngày trong tương lai");
        }

        Path file = duongDanFileTheoNgay(ngayThucTe);
        if (!Files.exists(file)) {
            return new PhanHoiNhatKy(ngayThucTe, false, 0, List.of());
        }

        Deque<String> cuaSoTruot = new ArrayDeque<>(soDongThucTe);
        try (Stream<String> dongFile = Files.lines(file, StandardCharsets.UTF_8)) {
            dongFile.filter(dong -> tuKhoaChuanHoa == null || dong.toLowerCase().contains(tuKhoaChuanHoa))
                    .forEach(dong -> {
                        if (cuaSoTruot.size() == soDongThucTe) {
                            cuaSoTruot.pollFirst();
                        }
                        cuaSoTruot.addLast(dong);
                    });
        } catch (IOException e) {
            log.error("Không đọc được file nhật ký {}", file, e);
            throw new NgoaiLeDuLieuKhongHopLe("Không thể đọc file nhật ký, vui lòng thử lại sau");
        }

        List<MucNhatKy> danhSach = cuaSoTruot.stream()
                .map(this::phanTichDong)
                .toList();

        return new PhanHoiNhatKy(ngayThucTe, true, danhSach.size(), danhSach);
    }

    /**
     * File hôm nay luôn là logs/audit.log (đang ghi dở); các ngày trước đã bị
     * TimeBasedRollingPolicy đổi tên thành logs/audit-{yyyy-MM-dd}.log — xem
     * fileNamePattern trong logback-spring.xml.
     */
    private Path duongDanFileTheoNgay(LocalDate ngay) {
        Path thuMucLog = Path.of(thuMucLogCauHinh);
        if (ngay.isEqual(LocalDate.now())) {
            return thuMucLog.resolve("audit.log");
        }
        return thuMucLog.resolve("audit-" + ngay.format(DINH_DANG_TEN_FILE) + ".log");
    }

    private MucNhatKy phanTichDong(String dong) {
        Matcher m = MAU_DONG_LOG.matcher(dong);
        if (!m.matches()) {
            // Phòng trường hợp có dòng không đúng định dạng chuẩn (vd log lỗi
            // I/O chen giữa) — vẫn trả về thay vì làm rớt cả API.
            return new MucNhatKy(null, null, null, null, dong);
        }
        return new MucNhatKy(
                m.group("thoiGian"),
                m.group("actor"),
                m.group("action"),
                m.group("target"),
                m.group("detail"));
    }
}