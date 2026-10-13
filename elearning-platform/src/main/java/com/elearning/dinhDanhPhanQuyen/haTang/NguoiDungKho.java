package com.elearning.dinhDanhPhanQuyen.haTang;

import com.elearning.dinhDanhPhanQuyen.mienDomain.NguoiDung;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.VaiTro;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.TrangThaiTaiKhoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * PUBLIC trong phạm vi cần thiết: Repository phải public để
 * DangKyDichVuImpl (package ungDung.impl) và XacThucDichVuImpl truy cập
 * được — Java package-private chỉ work trong CÙNG package, không xuyên
 * được qua "haTang" -> "ungDung.impl" dù cùng module.
 *
 * Ranh giới module ở đây được enforce bằng QUY ƯỚC + ArchUnit test
 * (xem KienTrucModuleTest ở src/test), KHÔNG dựa vào access modifier:
 * không module nào ngoài dinhDanhPhanQuyen được phép import bất kỳ
 * class nào trong package "...dinhDanhPhanQuyen.haTang".
 */
public interface NguoiDungKho extends JpaRepository<NguoiDung, Long> {

    Optional<NguoiDung> findByEmail(String email);

    boolean existsByEmail(String email);

    // BỔ SUNG HÀM NÀY ĐỂ XỬ LÝ TÌM KIẾM CHO ADMIN
    @Query("SELECT n FROM NguoiDung n WHERE " +
           // Nếu timKiem rỗng thì bỏ qua, nếu có thì tìm kiếm
           "(:timKiem = '' OR LOWER(n.hoTen) LIKE :timKiem OR LOWER(n.email) LIKE :timKiem) " +
           // Ép Enum về String và kiểm tra chuỗi rỗng
           "AND (:vaiTro = '' OR CAST(n.vaiTro AS String) = :vaiTro) " +
           "AND (:trangThai = '' OR CAST(n.trangThai AS String) = :trangThai)")
    Page<NguoiDung> timKiemNguoiDung(
            @Param("timKiem") String timKiem,
            @Param("vaiTro") String vaiTro,
            @Param("trangThai") String trangThai,
            Pageable pageable);

    /**
     * Dùng để gửi thông báo hàng loạt cho toàn bộ quản trị viên đúng vai trò
     * (vd: báo QTV_NOI_DUNG có khóa học mới cần duyệt) — chỉ lấy tài khoản
     * đang HOAT_DONG, tránh gửi cho tài khoản đã bị khóa/chưa kích hoạt.
     */
    @Query("SELECT n.maNguoiDung FROM NguoiDung n WHERE n.vaiTro = :vaiTro AND n.trangThai = :trangThai")
    List<Long> layMaTheoVaiTro(@Param("vaiTro") VaiTro vaiTro, @Param("trangThai") TrangThaiTaiKhoan trangThai);
}