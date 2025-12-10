package pl.su.su_backend.service.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.dashboard.DashboardSummaryResponseDto;
import pl.su.su_backend.dto.dashboard.MembershipDto;
import pl.su.su_backend.dto.dashboard.UserEventDto;
import pl.su.su_backend.dto.dashboard.UserProfileDataDto;
import pl.su.su_backend.dto.event.EventResponseDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.budget.CouncilBudget;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.EventStatus;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.SuggestionStatus;
import pl.su.su_backend.repositories.budget.CouncilBudgetRepository;
import pl.su.su_backend.repositories.council.CouncilMemberRepository;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.event.EventRepository;
import pl.su.su_backend.repositories.suggestion.SuggestionRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.event.EventService;
import pl.su.su_backend.service.user.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final CouncilBudgetRepository budgetRepository;
    private final SuggestionRepository suggestionRepository;
    private final EventRepository eventRepository;
    private final PermissionService permissionService;
    private final UserService userService;
    private final EventService eventService;

    @Transactional(readOnly = true)
    public DashboardSummaryResponseDto getDashboardSummary(UUID userId) {
        Optional<Council> defaultCouncilOpt = councilRepository.findFirstByActiveTrueAndDefaultCouncilTrue();

        DashboardSummaryResponseDto.DashboardSummaryResponseDtoBuilder builder = DashboardSummaryResponseDto.builder();

        long myTotal = suggestionRepository.countByUserId(userId);
        long myPending = suggestionRepository.countByUserIdAndStatus(userId, SuggestionStatus.PENDING);

        builder.myTotalSuggestionsCount(myTotal)
                .myPendingSuggestionsCount(myPending);

        if (defaultCouncilOpt.isEmpty()) {
            return builder.isCouncilMember(false).build();
        }

        Council council = defaultCouncilOpt.get();
        builder.activeCouncilId(council.getId())
                .activeCouncilName(council.getName());

        Optional<CouncilMember> memberOpt = councilMemberRepository.findByCouncilIdAndUserId(council.getId(), userId);
        boolean isMember = memberOpt.isPresent();

        builder.isCouncilMember(isMember);

        if (isMember) {
            if (permissionService.hasPermission(userId, PermissionCode.COUNCIL_BUDGET_VIEW, council.getId())) {
                BigDecimal balance = budgetRepository.findByCouncilId(council.getId())
                        .map(CouncilBudget::getBalance)
                        .orElse(BigDecimal.ZERO);
                builder.budgetBalance(balance);
            } else {
                builder.budgetBalance(null);
            }

            long pendingCount = suggestionRepository.countByCouncilIdAndStatus(council.getId(), SuggestionStatus.PENDING);
            builder.pendingSuggestionsCount(pendingCount);

            long eventsCount = eventRepository.countByCouncilIdAndStatusAndStartDateAfter(council.getId(), EventStatus.PENDING, LocalDateTime.now());
            builder.upcomingEventsCount(eventsCount);

        }

        return builder.build();
    }

    @Transactional(readOnly = true)
    public UserProfileDataDto getUserProfileData(UUID userId) {
        UserResponseDto userDto = userService.getUserById(userId);

        List<CouncilMember> allMemberships = councilMemberRepository.findAllByUserId(userId);

        List<MembershipDto> membershipDtos = allMemberships.stream()
                .map(member -> MembershipDto.builder()
                        .councilId(member.getCouncil().getId())
                        .councilName(member.getCouncil().getName())
                        .userRole(member.getRole().name())
                        .isActive(member.getCouncil().isActive())
                        .startDate(member.getCouncil().getStartDate())
                        .endDate(member.getCouncil().getEndDate())
                        .build())
                .collect(Collectors.toList());

        List<EventResponseDto> rawUserEvents = eventService.getUserEvents(userId);

        List<UserEventDto> userEventsDtos = rawUserEvents.stream()
                .map(eventDto -> UserEventDto.builder()
                        .eventId(eventDto.getId())
                        .title(eventDto.getTitle())
                        .startDate(eventDto.getStartDate())
                        .endDate(eventDto.getEndDate())
                        .build())
                .collect(Collectors.toList());


        long totalSuggestions = suggestionRepository.countByUserId(userId);
        long pendingSuggestions = suggestionRepository.countByUserIdAndStatus(userId, SuggestionStatus.PENDING);
        long approvedSuggestions = suggestionRepository.countByUserIdAndStatus(userId, SuggestionStatus.APPROVED);

        return UserProfileDataDto.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .fullName(userDto.getFullName())
                .status(userDto.getStatus())
                .globalRoles(userDto.getRoles())
                .totalSuggestionsCount(totalSuggestions)
                .pendingSuggestionsCount(pendingSuggestions)
                .approvedSuggestionsCount(approvedSuggestions)
                .memberships(membershipDtos)
                .userEvents(userEventsDtos)
                .build();
    }
}