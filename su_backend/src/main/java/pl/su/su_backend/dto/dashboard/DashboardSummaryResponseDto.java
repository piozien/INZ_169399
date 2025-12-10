package pl.su.su_backend.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class DashboardSummaryResponseDto {

    private boolean isCouncilMember;

    private UUID activeCouncilId;
    private String activeCouncilName;

    private BigDecimal budgetBalance;
    private long pendingSuggestionsCount;
    private long upcomingEventsCount;

    // def user statistic
    private long myTotalSuggestionsCount;
    private long myPendingSuggestionsCount;
}