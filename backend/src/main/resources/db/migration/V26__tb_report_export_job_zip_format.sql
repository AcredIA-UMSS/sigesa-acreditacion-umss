-- V26: Add ZIP format support to tb_report_export_job check constraint (FSD-UC-032 / FEAT-11)
ALTER TABLE tb_report_export_job DROP CONSTRAINT IF EXISTS tb_report_export_job_format_check;
ALTER TABLE tb_report_export_job ADD CONSTRAINT tb_report_export_job_format_check 
    CHECK (format::text = ANY (ARRAY['XLSX'::character varying, 'CSV'::character varying, 'PDF'::character varying, 'ZIP'::character varying]::text[]));
