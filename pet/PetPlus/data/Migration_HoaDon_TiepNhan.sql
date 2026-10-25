/*
   Extend HoaDon so a pet intake is stored in the invoice itself.
   This migration does not create a separate TiepNhanThuCung table.
   Run once against the existing PETPLUS database; statements are repeatable.
*/
USE PETPLUS;
GO

IF OBJECT_ID(N'dbo.HoaDon', N'U') IS NULL
BEGIN
    ;THROW 50001, N'Không tìm thấy bảng dbo.HoaDon trong PETPLUS.', 1;
END;
GO

IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_HoaDon_KhuyenMai')
    ALTER TABLE dbo.HoaDon DROP CONSTRAINT FK_HoaDon_KhuyenMai;
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_HoaDon_PhuongThucThanhToan')
    ALTER TABLE dbo.HoaDon DROP CONSTRAINT FK_HoaDon_PhuongThucThanhToan;
GO

ALTER TABLE dbo.HoaDon ALTER COLUMN maKhuyenMai VARCHAR(20) NULL;
ALTER TABLE dbo.HoaDon ALTER COLUMN maPTTT VARCHAR(20) NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_HoaDon_KhuyenMai')
    ALTER TABLE dbo.HoaDon ADD CONSTRAINT FK_HoaDon_KhuyenMai
        FOREIGN KEY (maKhuyenMai) REFERENCES dbo.KhuyenMai(maKhuyenMai);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_HoaDon_PhuongThucThanhToan')
    ALTER TABLE dbo.HoaDon ADD CONSTRAINT FK_HoaDon_PhuongThucThanhToan
        FOREIGN KEY (maPTTT) REFERENCES dbo.PhuongThucThanhToan(maPTTT);
GO

IF COL_LENGTH(N'dbo.HoaDon', N'laTiepNhan') IS NULL
    ALTER TABLE dbo.HoaDon ADD laTiepNhan BIT NOT NULL
        CONSTRAINT DF_HoaDon_LaTiepNhan DEFAULT (0);
IF COL_LENGTH(N'dbo.HoaDon', N'maLichHen') IS NULL
    ALTER TABLE dbo.HoaDon ADD maLichHen VARCHAR(20) NULL;
IF COL_LENGTH(N'dbo.HoaDon', N'maThuCung') IS NULL
    ALTER TABLE dbo.HoaDon ADD maThuCung VARCHAR(20) NULL;
IF COL_LENGTH(N'dbo.HoaDon', N'maNVTiepNhan') IS NULL
    ALTER TABLE dbo.HoaDon ADD maNVTiepNhan VARCHAR(20) NULL;
IF COL_LENGTH(N'dbo.HoaDon', N'thoiGianTiepNhan') IS NULL
    ALTER TABLE dbo.HoaDon ADD thoiGianTiepNhan DATETIME2 NULL;
IF COL_LENGTH(N'dbo.HoaDon', N'gioHenTra') IS NULL
    ALTER TABLE dbo.HoaDon ADD gioHenTra DATETIME2 NULL;
IF COL_LENGTH(N'dbo.HoaDon', N'dichVuYeuCau') IS NULL
    ALTER TABLE dbo.HoaDon ADD dichVuYeuCau NVARCHAR(500) NULL;
IF COL_LENGTH(N'dbo.HoaDon', N'tinhTrangBanGiao') IS NULL
    ALTER TABLE dbo.HoaDon ADD tinhTrangBanGiao NVARCHAR(1000) NULL;
IF COL_LENGTH(N'dbo.HoaDon', N'ghiChuTiepNhan') IS NULL
    ALTER TABLE dbo.HoaDon ADD ghiChuTiepNhan NVARCHAR(1000) NULL;
IF COL_LENGTH(N'dbo.HoaDon', N'phiDuKien') IS NULL
    ALTER TABLE dbo.HoaDon ADD phiDuKien DECIMAL(18,2) NULL;
IF COL_LENGTH(N'dbo.HoaDon', N'trangThaiTiepNhan') IS NULL
    ALTER TABLE dbo.HoaDon ADD trangThaiTiepNhan NVARCHAR(50) NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_HoaDon_LichHen')
    ALTER TABLE dbo.HoaDon ADD CONSTRAINT FK_HoaDon_LichHen
        FOREIGN KEY (maLichHen) REFERENCES dbo.LichHen(maLichHen);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_HoaDon_ThuCung')
    ALTER TABLE dbo.HoaDon ADD CONSTRAINT FK_HoaDon_ThuCung
        FOREIGN KEY (maThuCung) REFERENCES dbo.ThuCung(maThuCung);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_HoaDon_NVTiepNhan')
    ALTER TABLE dbo.HoaDon ADD CONSTRAINT FK_HoaDon_NVTiepNhan
        FOREIGN KEY (maNVTiepNhan) REFERENCES dbo.NhanVien(maNV);
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_HoaDon_PhiDuKien')
    ALTER TABLE dbo.HoaDon ADD CONSTRAINT CK_HoaDon_PhiDuKien
        CHECK (phiDuKien IS NULL OR phiDuKien >= 0);
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_HoaDon_ThoiGianTiepNhan')
    ALTER TABLE dbo.HoaDon ADD CONSTRAINT CK_HoaDon_ThoiGianTiepNhan CHECK (
        laTiepNhan = 0 OR (thoiGianTiepNhan IS NOT NULL AND gioHenTra IS NOT NULL
            AND gioHenTra > thoiGianTiepNhan
            AND maThuCung IS NOT NULL AND maNVTiepNhan IS NOT NULL AND trangThaiTiepNhan IS NOT NULL));
GO

SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET QUOTED_IDENTIFIER ON;
SET NUMERIC_ROUNDABORT OFF;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE object_id = OBJECT_ID(N'dbo.HoaDon')
                 AND name = N'UX_HoaDon_TiepNhan_LichHen')
    CREATE UNIQUE INDEX UX_HoaDon_TiepNhan_LichHen
        ON dbo.HoaDon(maLichHen) WHERE laTiepNhan = 1 AND maLichHen IS NOT NULL;
GO
