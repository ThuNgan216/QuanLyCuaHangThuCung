/*
   Thêm ràng buộc mỗi số điện thoại chỉ thuộc một khách hàng.
   Script này dùng cho database PETPLUS đã tồn tại; không xóa hoặc tạo lại database.
   Nếu dữ liệu hiện tại có số điện thoại trùng, cần xử lý các bản ghi đó trước.
*/
USE PETPLUS;
GO

IF OBJECT_ID(N'dbo.KhachHang', N'U') IS NULL
BEGIN
    ;THROW 50001, N'Không tìm thấy bảng dbo.KhachHang trong database PETPLUS.', 1;
END;

IF EXISTS (
    SELECT sdt
    FROM dbo.KhachHang
    WHERE sdt IS NOT NULL
    GROUP BY sdt
    HAVING COUNT(*) > 1
)
BEGIN
    ;THROW 50002, N'Có số điện thoại khách hàng bị trùng. Hãy xử lý dữ liệu trùng trước khi thêm ràng buộc.', 1;
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE name = N'UQ_KhachHang_SDT'
      AND parent_object_id = OBJECT_ID(N'dbo.KhachHang')
)
BEGIN
    ALTER TABLE dbo.KhachHang
        ADD CONSTRAINT UQ_KhachHang_SDT UNIQUE (sdt);
END;
