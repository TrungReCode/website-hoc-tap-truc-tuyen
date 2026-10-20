package com.elearning.dungChung.cauHoi;

import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Quy tắc nghiệp vụ DÙNG CHUNG cho mọi dạng câu hỏi của cả 2 hệ thống quiz
 * (quiz của khóa học gốc ở choKhoaHoc và quiz bổ sung ở lopHocLMS): kiểm tra
 * câu hỏi giảng viên soạn có hợp lệ không, chấm điểm một câu, và che các
 * thông tin lộ đáp án trước khi gửi đề cho học viên.
 *
 * Cố ý là lớp Java THUẦN (không Spring, không DB, không phụ thuộc kiểu
 * enum/DTO của từng module — loại câu hỏi truyền vào dạng String) để:
 * (1) cả 2 module cùng dùng được mà không phụ thuộc chéo lẫn nhau, và
 * (2) test được bằng unit test thuần, nhanh, không cần dựng ngữ cảnh nào.
 *
 * 6 dạng câu hỏi, tất cả chấm tự động:
 *   MOT_DAP_AN, NHIEU_DAP_AN, DUNG_SAI — chọn phương án (như trước đây)
 *   TRA_LOI_NGAN — học viên gõ câu trả lời; đúng nếu khớp MỘT trong các đáp
 *                  án chấp nhận (giảng viên nhập nhiều cách viết)
 *   SAP_XEP      — sắp xếp các mục theo đúng thứ tự; thứ tự đúng = thứ tự
 *                  giảng viên nhập (đề gửi cho học viên luôn được xáo trộn)
 *   GHEP_CAP     — nối từng mục vế trái với đúng mục vế phải
 *
 * Cách tái dùng cấu trúc đáp án (JSONB) có sẵn — KHÔNG cần đổi schema CSDL:
 *   TRA_LOI_NGAN: mỗi phần tử = 1 đáp án được chấp nhận (noiDung)
 *   SAP_XEP     : các phần tử theo ĐÚNG thứ tự (vị trí trong danh sách)
 *   GHEP_CAP    : noiDung = vế trái, noiDungGhep = vế phải tương ứng
 */
public final class QuyTacCauHoi {

    public static final String MOT_DAP_AN = "MOT_DAP_AN";
    public static final String NHIEU_DAP_AN = "NHIEU_DAP_AN";
    public static final String DUNG_SAI = "DUNG_SAI";
    public static final String TRA_LOI_NGAN = "TRA_LOI_NGAN";
    public static final String SAP_XEP = "SAP_XEP";
    public static final String GHEP_CAP = "GHEP_CAP";

    private static final Set<String> LOAI_HOP_LE = Set.of(
            MOT_DAP_AN, NHIEU_DAP_AN, DUNG_SAI, TRA_LOI_NGAN, SAP_XEP, GHEP_CAP);

    private QuyTacCauHoi() {
    }

    /** Một phần tử đáp án ở dạng trung lập, không phụ thuộc kiểu của module nào. */
    public record DapAn(Integer id, String noiDung, String noiDungGhep, boolean dapAnDung) {
    }

    /** Một cặp học viên đã nối (GHEP_CAP): mục trái {@code maDapAn} nối với vế phải {@code noiDungGhep}. */
    public record CapGhep(Integer maDapAn, String noiDungGhep) {
    }

    /** Bài làm của học viên cho 1 câu — chỉ trường tương ứng với dạng câu hỏi mới có ý nghĩa. */
    public record BaiLam(List<Integer> dsMaDapAn, String vanBan, List<CapGhep> capGhep) {
        public BaiLam {
            dsMaDapAn = dsMaDapAn == null ? List.of() : dsMaDapAn;
            capGhep = capGhep == null ? List.of() : capGhep;
        }
    }

    // ═══════════════════════ Kiểm tra câu hỏi do giảng viên soạn ═══════════════════════

    /**
     * Ném NgoaiLeDuLieuKhongHopLe (HTTP 400) nếu câu hỏi không hợp lệ theo dạng
     * của nó. {@code loai == null} coi như MOT_DAP_AN (hành vi cũ).
     */
    public static void kiemTraHopLe(String loai, List<DapAn> dapAn) {
        String l = loaiChuan(loai);
        if (!LOAI_HOP_LE.contains(l)) {
            throw new NgoaiLeDuLieuKhongHopLe("Loại câu hỏi không hợp lệ: " + loai);
        }
        if (dapAn == null || dapAn.isEmpty()) {
            throw new NgoaiLeDuLieuKhongHopLe("Câu hỏi cần có phương án trả lời");
        }
        Set<Integer> idDaGap = new HashSet<>();
        for (DapAn d : dapAn) {
            if (d.id() == null || !idDaGap.add(d.id())) {
                throw new NgoaiLeDuLieuKhongHopLe("Mã các phương án phải có giá trị và không được trùng nhau");
            }
        }

        long soDung = dapAn.stream().filter(DapAn::dapAnDung).count();
        switch (l) {
            case MOT_DAP_AN, DUNG_SAI -> {
                if (DUNG_SAI.equals(l) && dapAn.size() != 2) {
                    throw new NgoaiLeDuLieuKhongHopLe("Câu hỏi Đúng-Sai phải có đúng 2 phương án");
                }
                canToiThieu(dapAn, 2, "Câu hỏi cần ít nhất 2 phương án trả lời");
                if (soDung != 1) {
                    throw new NgoaiLeDuLieuKhongHopLe("Câu hỏi 1 đáp án / Đúng-Sai phải có đúng 1 đáp án đúng");
                }
            }
            case NHIEU_DAP_AN -> {
                canToiThieu(dapAn, 2, "Câu hỏi cần ít nhất 2 phương án trả lời");
                if (soDung < 1) {
                    throw new NgoaiLeDuLieuKhongHopLe("Câu hỏi cần có ít nhất 1 đáp án đúng");
                }
            }
            case TRA_LOI_NGAN -> {
                for (DapAn d : dapAn) {
                    if (chuanHoaVanBan(d.noiDung()).isEmpty()) {
                        throw new NgoaiLeDuLieuKhongHopLe("Đáp án được chấp nhận không được để trống");
                    }
                }
            }
            case SAP_XEP -> {
                canToiThieu(dapAn, 2, "Câu hỏi sắp xếp cần ít nhất 2 mục");
                canKhacNhau(dapAn.stream().map(DapAn::noiDung).toList(),
                        "Các mục cần sắp xếp không được để trống hoặc trùng nhau");
            }
            case GHEP_CAP -> {
                canToiThieu(dapAn, 2, "Câu hỏi ghép cặp cần ít nhất 2 cặp");
                canKhacNhau(dapAn.stream().map(DapAn::noiDung).toList(),
                        "Các mục vế trái không được để trống hoặc trùng nhau");
                canKhacNhau(dapAn.stream().map(DapAn::noiDungGhep).toList(),
                        "Các mục vế phải không được để trống hoặc trùng nhau");
            }
            default -> throw new NgoaiLeDuLieuKhongHopLe("Loại câu hỏi không hợp lệ: " + loai);
        }
    }

    private static void canToiThieu(List<DapAn> dapAn, int toiThieu, String thongBao) {
        if (dapAn.size() < toiThieu) {
            throw new NgoaiLeDuLieuKhongHopLe(thongBao);
        }
    }

    private static void canKhacNhau(List<String> giaTri, String thongBao) {
        Set<String> daGap = new HashSet<>();
        for (String g : giaTri) {
            String chuan = chuanHoaVanBan(g);
            if (chuan.isEmpty() || !daGap.add(chuan)) {
                throw new NgoaiLeDuLieuKhongHopLe(thongBao);
            }
        }
    }

    // ═══════════════════════ Chấm điểm ═══════════════════════

    /** true nếu bài làm đúng hoàn toàn (không chấm điểm từng phần — mỗi câu hoặc đúng hoặc sai). */
    public static boolean chamDung(String loai, List<DapAn> dapAn, BaiLam baiLam) {
        if (dapAn == null || dapAn.isEmpty() || baiLam == null) {
            return false;
        }
        return switch (loaiChuan(loai)) {
            case TRA_LOI_NGAN -> chamTraLoiNgan(dapAn, baiLam);
            case SAP_XEP -> chamSapXep(dapAn, baiLam);
            case GHEP_CAP -> chamGhepCap(dapAn, baiLam);
            default -> chamLuaChon(dapAn, baiLam);
        };
    }

    private static boolean chamLuaChon(List<DapAn> dapAn, BaiLam baiLam) {
        if (baiLam.dsMaDapAn().stream().anyMatch(Objects::isNull)) {
            return false;
        }
        Set<Integer> idDung = new HashSet<>();
        for (DapAn d : dapAn) {
            if (d.dapAnDung()) {
                idDung.add(d.id());
            }
        }
        return baiLam.dsMaDapAn().size() == new HashSet<>(baiLam.dsMaDapAn()).size()
                && new HashSet<>(baiLam.dsMaDapAn()).equals(idDung);
    }

    private static boolean chamTraLoiNgan(List<DapAn> dapAn, BaiLam baiLam) {
        String traLoi = chuanHoaVanBan(baiLam.vanBan());
        if (traLoi.isEmpty()) {
            return false;
        }
        for (DapAn d : dapAn) {
            if (traLoi.equals(chuanHoaVanBan(d.noiDung()))) {
                return true;
            }
        }
        return false;
    }

    private static boolean chamSapXep(List<DapAn> dapAn, BaiLam baiLam) {
        if (baiLam.dsMaDapAn().stream().anyMatch(Objects::isNull)) {
            return false;
        }
        List<Integer> thuTuDung = new ArrayList<>();
        for (DapAn d : dapAn) {
            thuTuDung.add(d.id());
        }
        return thuTuDung.equals(baiLam.dsMaDapAn());
    }

    private static boolean chamGhepCap(List<DapAn> dapAn, BaiLam baiLam) {
        if (baiLam.capGhep().size() != dapAn.size()) {
            return false;
        }
        Map<Integer, String> traLoiTheoMuc = new HashMap<>();
        for (CapGhep c : baiLam.capGhep()) {
            if (c == null || c.maDapAn() == null || c.noiDungGhep() == null) {
                return false;
            }
            if (traLoiTheoMuc.put(c.maDapAn(), chuanHoaVanBan(c.noiDungGhep())) != null) {
                return false; // cùng 1 mục trái bị nối 2 lần
            }
        }
        for (DapAn d : dapAn) {
            String daNoi = traLoiTheoMuc.get(d.id());
            if (daNoi == null || !daNoi.equals(chuanHoaVanBan(d.noiDungGhep()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Chuẩn hóa để so khớp câu trả lời gõ tay: Unicode NFC (cùng 1 chữ có dấu
     * dù gõ kiểu dựng sẵn hay tổ hợp), bỏ khoảng trắng thừa, không phân biệt
     * hoa/thường. KHÔNG bỏ dấu tiếng Việt — "ma" và "má" khác nghĩa; muốn
     * chấp nhận cả cách gõ không dấu, giảng viên thêm nó làm 1 đáp án chấp nhận.
     */
    public static String chuanHoaVanBan(String s) {
        if (s == null) {
            return "";
        }
        String n = Normalizer.normalize(s, Normalizer.Form.NFC).trim().replaceAll("\\s+", " ");
        return n.toLowerCase(Locale.ROOT);
    }

    // ═══════════════════════ Đề gửi cho học viên (chống lộ đáp án) ═══════════════════════

    /**
     * Danh sách phương án AN TOÀN để gửi cho học viên khi đang làm bài — không
     * mang cờ đúng/sai và không để lộ đáp án qua chính cấu trúc dữ liệu:
     *   TRA_LOI_NGAN: rỗng (noiDung của mỗi phần tử CHÍNH LÀ đáp án chấp nhận)
     *   SAP_XEP     : bị xáo trộn (thứ tự lưu = thứ tự đúng)
     *   GHEP_CAP    : chỉ vế trái (vế phải gửi riêng đã xáo trộn, xem luaChonGhep)
     */
    public static List<DapAn> anDapAnChoHocVien(String loai, List<DapAn> dapAn) {
        String l = loaiChuan(loai);
        if (TRA_LOI_NGAN.equals(l)) {
            return List.of();
        }
        List<DapAn> an = new ArrayList<>();
        for (DapAn d : dapAn) {
            an.add(new DapAn(d.id(), d.noiDung(), null, false));
        }
        return SAP_XEP.equals(l) ? xaoTronKhacThuTuGoc(an) : an;
    }

    /** Vế phải của câu GHEP_CAP, đã xáo trộn để không suy ra được cặp đúng; rỗng với dạng khác. */
    public static List<String> luaChonGhepChoHocVien(String loai, List<DapAn> dapAn) {
        if (!GHEP_CAP.equals(loaiChuan(loai))) {
            return List.of();
        }
        List<String> veTrai = new ArrayList<>();
        for (DapAn d : dapAn) {
            veTrai.add(d.noiDungGhep());
        }
        return xaoTronKhacThuTuGoc(veTrai);
    }

    /** Xáo trộn, cố gắng để kết quả KHÁC thứ tự gốc (tránh đề "đã sắp xếp sẵn" khi gặp may). */
    public static <T> List<T> xaoTronKhacThuTuGoc(List<T> goc) {
        List<T> ketQua = new ArrayList<>(goc);
        if (ketQua.size() < 2) {
            return ketQua;
        }
        for (int lan = 0; lan < 10; lan++) {
            Collections.shuffle(ketQua, ThreadLocalRandom.current());
            if (!ketQua.equals(goc)) {
                return ketQua;
            }
        }
        Collections.rotate(ketQua, 1); // phương án chắc chắn khi các phần tử gần như giống nhau
        return ketQua;
    }

    private static String loaiChuan(String loai) {
        return loai == null || loai.isBlank() ? MOT_DAP_AN : loai;
    }
}
