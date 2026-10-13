-- V16__them_bang_xac_thuc_giang_vien.sql
-- Thêm cơ chế xác thực chuyên môn cho người muốn trở thành tác giả khóa học:
-- phải >= 18 tuổi, khai báo bằng cấp/chứng chỉ chuyên môn kèm file minh
-- chứng, được Quản trị viên nội dung duyệt trước khi được phép tạo khóa học.
--
-- 1 người dùng chỉ có ĐÚNG 1 bản ghi (UNIQUE ma_nguoi_dung) — gửi lại sau khi
-- bị từ chối sẽ UPDATE lại bản ghi cũ, không tạo bản ghi mới, để giữ đúng 1
-- nguồn sự thật duy nhất về trạng thái xác thực của 1 người.
--
-- trang_thai dùng VARCHAR thay vì Postgres native enum — theo đúng tiền lệ
-- đã chọn ở giay_xac_nhan_ca_nhan/lop_hoc (V13), tránh lặp lại lớp phức tạp
-- CAST/@JdbcTypeCode của native enum cho một cột trạng thái đơn giản.

CREATE TABLE xac_thuc_giang_vien (
    ma_xac_thuc          BIGSERIAL     PRIMARY KEY,
    ma_nguoi_dung        BIGINT        NOT NULL UNIQUE REFERENCES nguoi_dung(ma_nguoi_dung),

    ngay_sinh            DATE          NOT NULL,
    bang_cap             TEXT          NOT NULL,

    -- Lưu ĐƯỜNG DẪN LƯU TRỮ nội bộ (không phải URL công khai) — file minh
    -- chứng (bằng cấp/CCCD) chứa dữ liệu cá nhân nhạy cảm, chỉ Admin đã xác
    -- thực quyền mới tải được qua API backend (xem AdapterLuuTruHoSoXacThuc),
    -- không có URL public nào tồn tại.
    duong_dan_luu_tru    TEXT          NOT NULL,
    ten_file_goc         VARCHAR(255),

    trang_thai           VARCHAR(20)   NOT NULL DEFAULT 'CHO_DUYET',
    ly_do_tu_choi        TEXT,
    ma_admin_xu_ly       BIGINT        REFERENCES nguoi_dung(ma_nguoi_dung),

    ngay_gui             TIMESTAMP     NOT NULL DEFAULT NOW(),
    ngay_xu_ly           TIMESTAMP
);

CREATE INDEX idx_xtgv_nguoi_dung ON xac_thuc_giang_vien(ma_nguoi_dung);
CREATE INDEX idx_xtgv_trang_thai ON xac_thuc_giang_vien(trang_thai);
