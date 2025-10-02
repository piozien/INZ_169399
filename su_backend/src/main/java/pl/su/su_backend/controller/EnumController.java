package pl.su.su_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.su.su_backend.model.enums.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/enums")
public class EnumController {
	@GetMapping
	public Map<String, Object> getAllEnums() {
		Map<String, Object> enums = new HashMap<>();
		
		enums.put("statuses", StatusEnum.values());
		enums.put("authProviders", AuthProvider.values());
		enums.put("roleCategories", RoleCategory.values());
		enums.put("roleCodes", RoleCode.values());
		enums.put("transactionTypes", TransactionType.values());
		enums.put("eventParticipantRoles", EventParticipantRole.values());
		enums.put("suggestionStatuses", SuggestionStatus.values());
		enums.put("actionTypes", ActionType.values());
		
		return enums;
	}

	@GetMapping("/statuses")
	public StatusEnum[] getStatuses() {
		return StatusEnum.values();
	}

	@GetMapping("/auth-providers")
	public AuthProvider[] getAuthProviders() {
		return AuthProvider.values();
	}

	@GetMapping("/role-categories")
	public RoleCategory[] getRoleCategories() {
		return RoleCategory.values();
	}

	@GetMapping("/role-codes")
	public RoleCode[] getRoleCodes() {
		return RoleCode.values();
	}

	@GetMapping("/transaction-types")
	public TransactionType[] getTransactionTypes() {
		return TransactionType.values();
	}

	@GetMapping("/event-participant-roles")
	public EventParticipantRole[] getEventParticipantRoles() {
		return EventParticipantRole.values();
	}

	@GetMapping("/suggestion-statuses")
	public SuggestionStatus[] getSuggestionStatuses() {
		return SuggestionStatus.values();
	}

	@GetMapping("/action-types")
	public ActionType[] getActionTypes() {
		return ActionType.values();
	}
}

