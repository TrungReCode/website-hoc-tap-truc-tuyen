package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.LopHoc;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface LopHocKho extends JpaRepository<LopHoc, Long> {

    Optional<LopHoc> findByMaLop(String maLop);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT l FROM LopHoc l WHERE l.maLop = :maLop")
        Optional<LopHoc> timKhoaVaKhoaTheoMaLop(@Param("maLop") String maLop);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT l FROM LopHoc l WHERE l.maLopHoc = :maLopHoc")
        Optional<LopHoc> timKhoaVaKhoaTheoMaLopHoc(@Param("maLopHoc") Long maLopHoc);

    boolean existsByMaLop(String maLop);

    List<LopHoc> findByMaGiangVien(Long maGiangVien);

    List<LopHoc> findByMaKhoaHoc(Long maKhoaHoc);

    /**
     * Duyệt công khai (không cần đăng nhập) — trang "Danh sách lớp học đang
     * hoạt động", tương tự Chợ Khóa Học. Chỉ trả về lớp congKhai=true và
     * chưa đóng (SAP_DIEN_RA hoặc HOAT_DONG); truyền danh sách trạng thái
     * qua tham số thay vì literal enum trong JPQL để không phải sửa câu
     * query khi nghiệp vụ đổi tập trạng thái "đang hoạt động".
     *
     * Tìm theo từ khóa lọc trên TÊN KHÓA HỌC (không phải mã lớp — mã lớp
     * là chuỗi ngẫu nhiên 8 ký tự, học viên chưa vào lớp sẽ không biết để
     * tìm). Vì LopHoc không lưu tên khóa học (thuộc module choKhoaHoc),
     * việc "tên khớp từ khóa" được giải quyết ở tầng Service bằng cách gọi
     * TimKiemKhoaHocDichVu để lấy trước danh sách maKhoaHoc phù hợp, rồi
     * truyền vào đây qua dsMaKhoaHocKhopTuKhoa — lọc bằng cờ coLocTuKhoa
     * thay vì kiểm tra "list IS NULL" để tránh JPQL suy nhầm kiểu tham số
     * khi cùng lúc dùng làm điều kiện IN.
     *
     * maKhoaHoc/gia đều optional — dùng CAST(... AS ...) IS NULL cho nhánh
     * optional, giống quy ước của timKiemChoAdmin phía trên (tránh lỗi
     * Postgres không suy được kiểu tham số 42P18).
     */
    @Query("SELECT l FROM LopHoc l " +
            "WHERE l.congKhai = true " +
            "AND l.trangThai IN :dsTrangThaiHoatDong " +
            "AND (CAST(:maKhoaHoc AS long) IS NULL OR l.maKhoaHoc = CAST(:maKhoaHoc AS long)) " +
            "AND (:coLocTuKhoa = false OR l.maKhoaHoc IN :dsMaKhoaHocKhopTuKhoa) " +
            "AND (CAST(:gia AS string) IS NULL " +
            "     OR (CAST(:gia AS string) = 'CO_PHI' AND l.phiLopHoc > 0) " +
            "     OR (CAST(:gia AS string) = 'MIEN_PHI' AND (l.phiLopHoc IS NULL OR l.phiLopHoc = 0)))")
    Page<LopHoc> timKiemCongKhai(@Param("dsTrangThaiHoatDong") List<TrangThaiLop> dsTrangThaiHoatDong,
            @Param("maKhoaHoc") Long maKhoaHoc,
            @Param("coLocTuKhoa") boolean coLocTuKhoa,
            @Param("dsMaKhoaHocKhopTuKhoa") List<Long> dsMaKhoaHocKhopTuKhoa,
            @Param("gia") String gia,
            Pageable pageable);

    /**
     * LƯU Ý: so sánh trangThai bằng String (không phải enum) để tránh lỗi
     * Postgres "could not determine data type of parameter" (SQLState 42P18)
     * khi tham số chỉ xuất hiện trong nhánh "IS NULL OR ..." — cùng cách xử
     * lý đã áp dụng ở DanhMucKho/KhoaHocKho.timKiemChoAdmin.
     */
    @Query("SELECT l FROM LopHoc l " +
            "WHERE (CAST(:trangThai AS string) IS NULL OR CAST(l.trangThai AS string) = :trangThai) " +
            "AND (CAST(:maLop AS string) IS NULL OR LOWER(l.maLop) LIKE LOWER(CONCAT('%', CAST(:maLop AS string), '%')))")
    Page<LopHoc> timKiemChoAdmin(@Param("trangThai") String trangThai,
            @Param("maLop") String maLop,
            Pageable pageable);

    @Modifying
    @Query("UPDATE LopHoc l SET l.trangThai = :trangThaiMoi WHERE l.trangThai = :trangThaiCu AND " +
            "((:kieuThoiGian = 'BAT_DAU' AND l.ngayBatDau <= :thoiDiemHienTai) OR " +
            " (:kieuThoiGian = 'KET_THUC' AND l.ngayKetThuc IS NOT NULL AND l.ngayKetThuc <= :thoiDiemHienTai))")
    void capNhatTrangThai(@Param("trangThaiCu") TrangThaiLop trangThaiCu,
            @Param("trangThaiMoi") TrangThaiLop trangThaiMoi,
            @Param("thoiDiemHienTai") LocalDateTime thoiDiemHienTai,
            @Param("kieuThoiGian") String kieuThoiGian);
}