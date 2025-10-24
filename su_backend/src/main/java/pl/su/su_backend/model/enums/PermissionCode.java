package pl.su.su_backend.model.enums;

import lombok.Getter;

@Getter
public enum PermissionCode {
    // User Management
    USER_VIEW("user.view", "View users"),
    USER_CREATE("user.create", "Create users"),
    USER_EDIT("user.edit", "Edit users"),
    USER_DELETE("user.delete", "Delete users"),
    USER_ASSIGN_ROLE("user.assign_role", "Assign roles to users"),

    // Class Management
    CLASS_VIEW("class.view", "View classes"),
    CLASS_CREATE("class.create", "Create classes"),
    CLASS_EDIT("class.edit", "Edit classes"),
    CLASS_DELETE("class.delete", "Delete classes"),

    // Class Budget Management
    CLASS_BUDGET_VIEW("class_budget.view", "View class budgets"),
    CLASS_BUDGET_CREATE("class_budget.create", "Create class budgets"),
    CLASS_BUDGET_EDIT("class_budget.edit", "Edit class budgets"),
    CLASS_BUDGET_DELETE("class_budget.delete", "Delete class budgets"),

    // Class Transaction Management
    CLASS_TRANSACTION_VIEW("class_transaction.view", "View class transactions"),
    CLASS_TRANSACTION_CREATE("class_transaction.create", "Create class transactions"),
    CLASS_TRANSACTION_EDIT("class_transaction.edit", "Edit class transactions"),
    CLASS_TRANSACTION_DELETE("class_transaction.delete", "Delete class transactions"),

    // Council Management
    COUNCIL_VIEW("council.view", "View council"),
    COUNCIL_VIEW_ALL("council.view_all", "View all councils"),
    COUNCIL_CREATE("council.create", "Create council"),
    COUNCIL_EDIT("council.edit", "Edit council"),
    COUNCIL_DELETE("council.delete", "Delete council"),
    COUNCIL_MEMBER_MANAGE("council_member.manage", "Manage council members"),

    // Council Budget Management
    COUNCIL_BUDGET_VIEW("council_budget.view", "View council budgets"),
    COUNCIL_BUDGET_CREATE("council_budget.create", "Create council budgets"),
    COUNCIL_BUDGET_EDIT("council_budget.edit", "Edit council budgets"),
    COUNCIL_BUDGET_DELETE("council_budget.delete", "Delete council budgets"),

    // Council Transaction Management
    COUNCIL_TRANSACTION_VIEW("council_transaction.view", "View council transactions"),
    COUNCIL_TRANSACTION_CREATE("council_transaction.create", "Create council transactions"),
    COUNCIL_TRANSACTION_EDIT("council_transaction.edit", "Edit council transactions"),
    COUNCIL_TRANSACTION_DELETE("council_transaction.delete", "Delete council transactions"),

    // Event Management
    EVENT_VIEW("event.view", "View events"),
    EVENT_VIEW_DRAFTS("event.view_drafts", "View draft events"),
    EVENT_CREATE("event.create", "Create events"),
    EVENT_EDIT("event.edit", "Edit events"),
    EVENT_DELETE("event.delete", "Delete events"),
    EVENT_APPROVE("event.approve", "Approve events"),

    // Suggestion Management
    SUGGESTION_VIEW("suggestion.view", "View suggestions"),
    SUGGESTION_VIEW_ANONYMOUS("suggestion.view_anonymous", "View anonymous suggestion authors"),
    SUGGESTION_CREATE("suggestion.create", "Create suggestions"),
    SUGGESTION_EDIT("suggestion.edit", "Edit suggestions"),
    SUGGESTION_DELETE("suggestion.delete", "Delete suggestions"),
    SUGGESTION_APPROVE("suggestion.approve", "Approve suggestions"),
    SUGGESTION_REJECT("suggestion.reject", "Reject suggestions"),

    // Report Management
    REPORT_VIEW("report.view", "View reports"),
    REPORT_GENERATE("report.generate", "Generate reports"),

    // Activity Log Management
    ACTIVITY_LOG_VIEW("activity_log.view", "View activity logs"),


    // Admin Management
    ADMIN("admin", "Full administrative access");

    private final String code;
    private final String description;

    PermissionCode(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
