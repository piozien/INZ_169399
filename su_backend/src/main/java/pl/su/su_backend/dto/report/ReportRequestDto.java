package pl.su.su_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reportType; // "SUMMARY", "DETAILED", "CATEGORY"
    private boolean includeTransactions;
    private boolean showPayerInfo;
}
