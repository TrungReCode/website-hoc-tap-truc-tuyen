-- V11: Bổ sung giá trị CHO_BO_SUNG cho enum trạng thái khóa học
--
-- Lý do: UC-11 (Kiểm duyệt nội dung) yêu cầu 3 nhánh quyết định: Duyệt /
-- Từ chối / Yêu cầu bổ sung. Nhánh "Yêu cầu bổ sung" cần một trạng thái
-- trung gian riêng (khác NHAP và CHO_KIEM_DUYET) để Giảng viên biết khóa
-- học đang chờ mình bổ sung theo góp ý của Quản trị viên, trước khi có
-- thể gửi lại xét duyệt.
--
-- LƯU Ý QUAN TRỌNG VỀ TÊN TYPE:
-- V2__tao_bang_khoa_hoc.sql khai báo type tên là "trang_thai_kh_enum", nhưng
-- ở một số database đã tồn tại từ trước (khi bảng khoa_hoc còn do Hibernate
-- ddl-auto tự sinh, trước khi Flyway quản lý toàn bộ schema), Postgres đang
-- lưu type này dưới tên "trangthaikhoahoc" (không dấu gạch dưới — theo quy
-- ước đặt tên mặc định của Hibernate dựa trên tên class Java). Vì Flyway
-- không chạy lại migration đã áp dụng (V2 đã success=true), database đó sẽ
-- không tự đổi tên type để khớp lại V2. Migration này vì vậy KHÔNG đoán cứng
-- một cái tên, mà tự dò xem type nào trong 2 tên đang thực sự tồn tại rồi
-- thao tác trên đúng type đó, để hoạt động đúng ở cả 2 kiểu database.
DO $$
DECLARE
    ten_type_thuc text;
BEGIN
    SELECT typname INTO ten_type_thuc
    FROM pg_type
    WHERE typname IN ('trang_thai_kh_enum', 'trangthaikhoahoc')
    LIMIT 1;

    IF ten_type_thuc IS NULL THEN
        -- Không tồn tại type nào cả (database hoàn toàn mới, đúng chuẩn V2) -> tạo mới.
        EXECUTE $sql$
            CREATE TYPE public.trang_thai_kh_enum AS ENUM
                ('NHAP', 'CHO_KIEM_DUYET', 'CHO_BO_SUNG', 'CONG_KHAI', 'LUU_TRU', 'NOI_BO')
        $sql$;
    ELSIF NOT EXISTS (
        SELECT 1
        FROM pg_enum e JOIN pg_type t ON t.oid = e.enumtypid
        WHERE t.typname = ten_type_thuc AND e.enumlabel = 'CHO_BO_SUNG'
    ) THEN
        -- Đã có type (dù tên nào trong 2 tên) nhưng chưa có giá trị CHO_BO_SUNG -> thêm vào.
        EXECUTE format('ALTER TYPE public.%I ADD VALUE %L', ten_type_thuc, 'CHO_BO_SUNG');
    END IF;
    -- Nếu đã có sẵn giá trị CHO_BO_SUNG rồi (chạy lại migration) -> không làm gì cả.
END$$;