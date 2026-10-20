package com.elearning.dungChung.cauHoi;

import com.elearning.dungChung.cauHoi.QuyTacCauHoi.BaiLam;
import com.elearning.dungChung.cauHoi.QuyTacCauHoi.CapGhep;
import com.elearning.dungChung.cauHoi.QuyTacCauHoi.DapAn;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test thuần cho quy tắc chung của mọi dạng câu hỏi: chấm điểm, kiểm tra
 * câu hỏi giảng viên soạn, và che đáp án trước khi gửi đề cho học viên. Không
 * cần Spring/DB nên chạy tức thì.
 */
class QuyTacCauHoiTest {

    private static DapAn chon(int id, String noiDung, boolean dung) {
        return new DapAn(id, noiDung, null, dung);
    }

    private static DapAn cap(int id, String trai, String phai) {
        return new DapAn(id, trai, phai, true);
    }

    private static BaiLam chonIds(Integer... ids) {
        return new BaiLam(List.of(ids), null, null);
    }

    private static BaiLam gonVanBan(String s) {
        return new BaiLam(null, s, null);
    }

    // ═════════════ Chọn phương án (hành vi cũ phải giữ nguyên) ═════════════

    @Test
    @DisplayName("1 đáp án: chọn đúng thì đúng, chọn sai hoặc bỏ trống thì sai")
    void motDapAn() {
        List<DapAn> ds = List.of(chon(1, "A", false), chon(2, "B", true), chon(3, "C", false));
        assertTrue(QuyTacCauHoi.chamDung("MOT_DAP_AN", ds, chonIds(2)));
        assertFalse(QuyTacCauHoi.chamDung("MOT_DAP_AN", ds, chonIds(1)));
        assertFalse(QuyTacCauHoi.chamDung("MOT_DAP_AN", ds, new BaiLam(null, null, null)));
        assertTrue(QuyTacCauHoi.chamDung(null, ds, chonIds(2)), "loại null được coi là MOT_DAP_AN");
    }

    @Test
    @DisplayName("Nhiều đáp án: phải chọn đủ và không chọn thừa")
    void nhieuDapAn() {
        List<DapAn> ds = List.of(chon(1, "A", true), chon(2, "B", true), chon(3, "C", false));
        assertTrue(QuyTacCauHoi.chamDung("NHIEU_DAP_AN", ds, chonIds(2, 1)));
        assertFalse(QuyTacCauHoi.chamDung("NHIEU_DAP_AN", ds, chonIds(1)));
        assertFalse(QuyTacCauHoi.chamDung("NHIEU_DAP_AN", ds, chonIds(1, 2, 3)));
        assertFalse(QuyTacCauHoi.chamDung("NHIEU_DAP_AN", ds, chonIds(1, 1, 2)),
                "không được lợi dụng mã trùng để vượt qua chấm điểm");
    }

    // ═════════════ Trả lời ngắn ═════════════

    @Test
    @DisplayName("Trả lời ngắn: không phân biệt hoa/thường và khoảng trắng thừa")
    void traLoiNganChuanHoa() {
        List<DapAn> ds = List.of(chon(1, "Hà Nội", true));
        assertTrue(QuyTacCauHoi.chamDung("TRA_LOI_NGAN", ds, gonVanBan("Hà Nội")));
        assertTrue(QuyTacCauHoi.chamDung("TRA_LOI_NGAN", ds, gonVanBan("hÀ nỘi")));
        assertTrue(QuyTacCauHoi.chamDung("TRA_LOI_NGAN", ds, gonVanBan("  Hà    Nội  ")));
    }

    @Test
    @DisplayName("Trả lời ngắn: dấu tiếng Việt có nghĩa, muốn nhận không dấu phải thêm làm đáp án chấp nhận")
    void traLoiNganDauTiengViet() {
        assertFalse(QuyTacCauHoi.chamDung("TRA_LOI_NGAN", List.of(chon(1, "má", true)), gonVanBan("ma")));
        List<DapAn> haiCach = List.of(chon(1, "Hà Nội", true), chon(2, "Ha Noi", true));
        assertTrue(QuyTacCauHoi.chamDung("TRA_LOI_NGAN", haiCach, gonVanBan("ha noi")));
        assertTrue(QuyTacCauHoi.chamDung("TRA_LOI_NGAN", List.of(chon(1, "đà nẵng", true)), gonVanBan("ĐÀ NẴNG")));
    }

    @Test
    @DisplayName("Trả lời ngắn: sai, rỗng, null đều sai")
    void traLoiNganSai() {
        List<DapAn> ds = List.of(chon(1, "Hà Nội", true));
        assertFalse(QuyTacCauHoi.chamDung("TRA_LOI_NGAN", ds, gonVanBan("Huế")));
        assertFalse(QuyTacCauHoi.chamDung("TRA_LOI_NGAN", ds, gonVanBan("   ")));
        assertFalse(QuyTacCauHoi.chamDung("TRA_LOI_NGAN", ds, gonVanBan(null)));
    }

    // ═════════════ Sắp xếp ═════════════

    @Test
    @DisplayName("Sắp xếp: chỉ đúng khi đúng thứ tự, đủ mục, không trùng")
    void sapXep() {
        List<DapAn> ds = List.of(chon(1, "Bước 1", true), chon(2, "Bước 2", true), chon(3, "Bước 3", true));
        assertTrue(QuyTacCauHoi.chamDung("SAP_XEP", ds, chonIds(1, 2, 3)));
        assertFalse(QuyTacCauHoi.chamDung("SAP_XEP", ds, chonIds(2, 1, 3)));
        assertFalse(QuyTacCauHoi.chamDung("SAP_XEP", ds, chonIds(1, 2)));
        assertFalse(QuyTacCauHoi.chamDung("SAP_XEP", ds, chonIds(1, 1, 2)));
        assertFalse(QuyTacCauHoi.chamDung("SAP_XEP", ds, new BaiLam(null, null, null)));
    }

    // ═════════════ Ghép cặp ═════════════

    private static final List<DapAn> GHEP = List.of(
            cap(1, "Java", "Ngôn ngữ"), cap(2, "Spring", "Framework"), cap(3, "Postgres", "CSDL"));

    private static BaiLam ghepCap(CapGhep... cacCap) {
        return new BaiLam(null, null, List.of(cacCap));
    }

    @Test
    @DisplayName("Ghép cặp: đúng hết thì đúng, kể cả khác thứ tự và hoa/thường")
    void ghepCapDung() {
        assertTrue(QuyTacCauHoi.chamDung("GHEP_CAP", GHEP,
                ghepCap(new CapGhep(1, "Ngôn ngữ"), new CapGhep(2, "Framework"), new CapGhep(3, "CSDL"))));
        assertTrue(QuyTacCauHoi.chamDung("GHEP_CAP", GHEP,
                ghepCap(new CapGhep(3, " csdl "), new CapGhep(1, "NGÔN NGỮ"), new CapGhep(2, "framework"))));
    }

    @Test
    @DisplayName("Ghép cặp: sai 1 cặp, thiếu cặp, nối 1 mục 2 lần, mã mục lạ, bỏ trống đều sai")
    void ghepCapSai() {
        assertFalse(QuyTacCauHoi.chamDung("GHEP_CAP", GHEP,
                ghepCap(new CapGhep(1, "Framework"), new CapGhep(2, "Ngôn ngữ"), new CapGhep(3, "CSDL"))));
        assertFalse(QuyTacCauHoi.chamDung("GHEP_CAP", GHEP,
                ghepCap(new CapGhep(1, "Ngôn ngữ"), new CapGhep(2, "Framework"))));
        assertFalse(QuyTacCauHoi.chamDung("GHEP_CAP", GHEP,
                ghepCap(new CapGhep(1, "Ngôn ngữ"), new CapGhep(1, "Ngôn ngữ"), new CapGhep(3, "CSDL"))));
        assertFalse(QuyTacCauHoi.chamDung("GHEP_CAP", GHEP,
                ghepCap(new CapGhep(1, "Ngôn ngữ"), new CapGhep(2, "Framework"), new CapGhep(99, "CSDL"))));
        assertFalse(QuyTacCauHoi.chamDung("GHEP_CAP", GHEP, new BaiLam(null, null, null)));
    }

    // ═════════════ Kiểm tra câu hỏi giảng viên soạn ═════════════

    @Test
    @DisplayName("Câu hỏi hợp lệ của cả 6 dạng đều được chấp nhận")
    void cauHoiHopLe() {
        QuyTacCauHoi.kiemTraHopLe(null, List.of(chon(1, "A", true), chon(2, "B", false)));
        QuyTacCauHoi.kiemTraHopLe("NHIEU_DAP_AN", List.of(chon(1, "A", true), chon(2, "B", true)));
        QuyTacCauHoi.kiemTraHopLe("DUNG_SAI", List.of(chon(1, "Đúng", true), chon(2, "Sai", false)));
        QuyTacCauHoi.kiemTraHopLe("TRA_LOI_NGAN", List.of(chon(1, "một", true)));
        QuyTacCauHoi.kiemTraHopLe("SAP_XEP", List.of(chon(1, "A", true), chon(2, "B", true)));
        QuyTacCauHoi.kiemTraHopLe("GHEP_CAP", GHEP);
    }

    private static void phaiTuChoi(String loai, List<DapAn> ds) {
        assertThrows(NgoaiLeDuLieuKhongHopLe.class, () -> QuyTacCauHoi.kiemTraHopLe(loai, ds));
    }

    @Test
    @DisplayName("Từ chối loại câu hỏi lạ, danh sách rỗng và mã phương án trùng")
    void tuChoiChung() {
        phaiTuChoi("TU_LUAN", List.of(chon(1, "A", true), chon(2, "B", false)));
        phaiTuChoi("MOT_DAP_AN", List.of());
        phaiTuChoi("MOT_DAP_AN", List.of(chon(1, "A", true), chon(1, "B", false)));
    }

    @Test
    @DisplayName("Câu 1 đáp án phải có ≥2 phương án và đúng 1 đáp án đúng")
    void tuChoiMotDapAn() {
        phaiTuChoi("MOT_DAP_AN", List.of(chon(1, "A", true)));
        phaiTuChoi("MOT_DAP_AN", List.of(chon(1, "A", true), chon(2, "B", true)));
        phaiTuChoi("MOT_DAP_AN", List.of(chon(1, "A", false), chon(2, "B", false)));
        phaiTuChoi("NHIEU_DAP_AN", List.of(chon(1, "A", false), chon(2, "B", false)));
        phaiTuChoi("DUNG_SAI", List.of(chon(1, "Đúng", true), chon(2, "Sai", false),
                chon(3, "Không chắc", false)));
    }

    @Test
    @DisplayName("Bài làm thiếu hoặc null không làm chấm điểm phát sinh lỗi")
    void baiLamKhongHopLe() {
        List<DapAn> ds = List.of(chon(1, "A", true), chon(2, "B", false));
        assertFalse(QuyTacCauHoi.chamDung("MOT_DAP_AN", ds, null));
        assertFalse(QuyTacCauHoi.chamDung("MOT_DAP_AN", null, chonIds(1)));
        assertFalse(QuyTacCauHoi.chamDung("MOT_DAP_AN", ds,
                new BaiLam(java.util.Arrays.asList((Integer) null), null, null)));
        assertFalse(QuyTacCauHoi.chamDung("GHEP_CAP",
                List.of(cap(1, "A", "B"), cap(2, "C", "D")),
                ghepCap(new CapGhep(1, null), new CapGhep(2, "D"))));
    }

    @Test
    @DisplayName("Trả lời ngắn không được có đáp án chấp nhận để trống")
    void tuChoiTraLoiNgan() {
        phaiTuChoi("TRA_LOI_NGAN", List.of(chon(1, "  ", true)));
    }

    @Test
    @DisplayName("Sắp xếp cần ≥2 mục khác nhau")
    void tuChoiSapXep() {
        phaiTuChoi("SAP_XEP", List.of(chon(1, "A", true)));
        phaiTuChoi("SAP_XEP", List.of(chon(1, "A", true), chon(2, " a ", true)));
    }

    @Test
    @DisplayName("Ghép cặp cần ≥2 cặp, vế trái và vế phải đều không trống và không trùng")
    void tuChoiGhepCap() {
        phaiTuChoi("GHEP_CAP", List.of(cap(1, "X", "Y")));
        phaiTuChoi("GHEP_CAP", List.of(cap(1, "X", "Y"), cap(2, "Z", "y")));
        phaiTuChoi("GHEP_CAP", List.of(cap(1, "X", "Y"), cap(2, "x", "Z")));
        phaiTuChoi("GHEP_CAP", List.of(cap(1, "X", null), cap(2, "Z", "W")));
    }

    // ═════════════ Chống lộ đáp án khi gửi đề cho học viên ═════════════

    @Test
    @DisplayName("Trả lời ngắn: không gửi bất kỳ đáp án nào (noiDung chính là đáp án chấp nhận)")
    void anTraLoiNgan() {
        List<DapAn> ds = List.of(chon(1, "Hà Nội", true));
        assertTrue(QuyTacCauHoi.anDapAnChoHocVien("TRA_LOI_NGAN", ds).isEmpty());
    }

    @Test
    @DisplayName("Ghép cặp: gửi vế trái không kèm vế phải, vế phải gửi riêng và đã xáo trộn")
    void anGhepCap() {
        List<DapAn> an = QuyTacCauHoi.anDapAnChoHocVien("GHEP_CAP", GHEP);
        assertEquals(3, an.size());
        assertTrue(an.stream().allMatch(d -> d.noiDungGhep() == null), "không được lộ vế phải ngay trong từng mục");
        assertTrue(an.stream().noneMatch(DapAn::dapAnDung));

        Set<String> vePhai = new HashSet<>(QuyTacCauHoi.luaChonGhepChoHocVien("GHEP_CAP", GHEP));
        assertEquals(Set.of("Ngôn ngữ", "Framework", "CSDL"), vePhai);
        assertTrue(QuyTacCauHoi.luaChonGhepChoHocVien("MOT_DAP_AN", GHEP).isEmpty());
    }

    @Test
    @DisplayName("Sắp xếp: đề gửi đi luôn khác thứ tự đúng và không mất mục")
    void anSapXep() {
        List<DapAn> ds = List.of(chon(1, "A", true), chon(2, "B", true), chon(3, "C", true));
        for (int lan = 0; lan < 200; lan++) {
            List<Integer> ids = QuyTacCauHoi.anDapAnChoHocVien("SAP_XEP", ds).stream().map(DapAn::id).toList();
            assertFalse(ids.equals(List.of(1, 2, 3)), "đề không được gửi sẵn theo thứ tự đúng");
            assertEquals(Set.of(1, 2, 3), new HashSet<>(ids));
        }
    }

    @Test
    @DisplayName("Dạng chọn phương án: giữ nguyên thứ tự, chỉ bỏ cờ đúng/sai")
    void anLuaChon() {
        List<DapAn> ds = List.of(chon(1, "A", false), chon(2, "B", true));
        List<DapAn> an = QuyTacCauHoi.anDapAnChoHocVien("MOT_DAP_AN", ds);
        assertEquals(1, an.get(0).id());
        assertTrue(an.stream().noneMatch(DapAn::dapAnDung));
    }
}
