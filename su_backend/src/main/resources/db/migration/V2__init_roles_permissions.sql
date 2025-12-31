INSERT INTO roles (role_code, description) VALUES
                                               ('DYREKTOR', 'Dyrektor szkoły'),
                                               ('ZASTEPCA_DYREKTORA', 'Zastępca dyrektora szkoły'),
                                               ('OPIEKUN_SU', 'Opiekun samorządu uczniowskiego'),
                                               ('NAUCZYCIEL', 'Nauczyciel'),
                                               ('PRZEWODNICZACY_SU', 'Przewodniczący SU'),
                                               ('ZASTEPCA_SU', 'Zastępca SU'),
                                               ('SKARBNIK_SU', 'Skarbnik SU'),
                                               ('CZLONEK_SU', 'Członek SU'),
                                               ('BYLY_CZLONEK_SU', 'Były członek SU'),
                                               ('UCZEN', 'Uczeń'),
                                               ('BYLY_UCZEN', 'Były uczeń'),
                                               ('ADMINISTRATOR', 'Administrator systemu')
    ON CONFLICT (role_code) DO NOTHING;

INSERT INTO permissions (name, description) VALUES
                                                -- User
                                                ('USER_VIEW', 'View users'), ('USER_CREATE', 'Create users'), ('USER_EDIT', 'Edit users'), ('USER_DELETE', 'Delete users'), ('USER_ASSIGN_ROLE', 'Assign roles'), ('ROLE_MANAGE', 'Manage roles'),
                                                -- Council
                                                ('COUNCIL_VIEW', 'View council'), ('COUNCIL_VIEW_ALL', 'View all councils'), ('COUNCIL_CREATE', 'Create council'), ('COUNCIL_EDIT', 'Edit council'), ('COUNCIL_DELETE', 'Delete council'), ('COUNCIL_MEMBER_MANAGE', 'Manage members'), ('COUNCIL_JOIN', 'Join council'),
                                                -- Budget
                                                ('COUNCIL_BUDGET_VIEW', 'View budgets'), ('COUNCIL_BUDGET_CREATE', 'Create budget year'), ('COUNCIL_BUDGET_EDIT', 'Edit budget settings'), ('COUNCIL_BUDGET_DELETE', 'Delete entire budget'),
                                                -- Transactions
                                                ('COUNCIL_TRANSACTION_VIEW', 'View transactions'), ('COUNCIL_TRANSACTION_CREATE', 'Create transaction'), ('COUNCIL_TRANSACTION_EDIT', 'Edit transaction'), ('COUNCIL_TRANSACTION_DELETE', 'Delete transaction'),
                                                -- Events & Suggestions & Reports
                                                ('EVENT_VIEW', 'View events'), ('EVENT_VIEW_DRAFTS', 'View drafts'), ('EVENT_CREATE', 'Create events'), ('EVENT_EDIT', 'Edit events'), ('EVENT_DELETE', 'Delete events'), ('EVENT_APPROVE', 'Approve events'),('EVENT_REMOVE_PARTICIPANTS', 'Remove participant events'),
                                                ('SUGGESTION_VIEW', 'View suggestions'), ('SUGGESTION_CREATE', 'Create suggestions'), ('SUGGESTION_EDIT', 'Edit'), ('SUGGESTION_DELETE', 'Delete'), ('SUGGESTION_APPROVE', 'Approve'), ('SUGGESTION_REJECT', 'Reject'),
                                                ('REPORT_VIEW', 'View reports'), ('REPORT_GENERATE', 'Generate reports'), ('ACTIVITY_LOG_VIEW', 'View logs')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.role_code IN ('ADMINISTRATOR', 'DYREKTOR')
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_code IN ('ZASTEPCA_DYREKTORA') AND p.name IN (
                                                                                     'USER_VIEW', 'USER_CREATE', 'USER_EDIT',
                                                                                     'COUNCIL_VIEW', 'COUNCIL_VIEW_ALL', 'COUNCIL_CREATE', 'COUNCIL_MEMBER_MANAGE','COUNCIL_DELETE',
                                                                                     'COUNCIL_BUDGET_VIEW', 'COUNCIL_BUDGET_CREATE', 'COUNCIL_BUDGET_EDIT', 'COUNCIL_BUDGET_DELETE',
                                                                                     'COUNCIL_TRANSACTION_VIEW', 'COUNCIL_TRANSACTION_CREATE', 'COUNCIL_TRANSACTION_EDIT', 'COUNCIL_TRANSACTION_DELETE',
                                                                                     'EVENT_VIEW', 'EVENT_VIEW_DRAFTS', 'EVENT_APPROVE', 'EVENT_DELETE','EVENT_REMOVE_PARTICIPANTS',
                                                                                     'SUGGESTION_VIEW', 'SUGGESTION_APPROVE', 'SUGGESTION_REJECT','SUGGESTION_DELETE','SUGGESTION_EDIT',
                                                                                     'REPORT_VIEW', 'REPORT_GENERATE', 'ACTIVITY_LOG_VIEW'
    )
    ON CONFLICT DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_code IN ('NAUCZYCIEL') AND p.name IN (
                                                                                     'USER_VIEW', 'USER_CREATE', 'USER_EDIT',
                                                                                     'COUNCIL_VIEW',
                                                                                     'COUNCIL_BUDGET_VIEW',
                                                                                     'COUNCIL_TRANSACTION_VIEW',
                                                                                     'EVENT_VIEW', 'EVENT_VIEW_DRAFTS',
                                                                                     'SUGGESTION_VIEW',
                                                                                     'REPORT_VIEW', 'REPORT_GENERATE'
    )
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_code IN ('OPIEKUN_SU') AND p.name IN (
                                                   'USER_VIEW', 'USER_CREATE', 'USER_EDIT',
                                                   'COUNCIL_VIEW', 'COUNCIL_CREATE', 'COUNCIL_MEMBER_MANAGE',
                                                   'COUNCIL_BUDGET_VIEW', 'COUNCIL_BUDGET_CREATE', 'COUNCIL_BUDGET_EDIT', 'COUNCIL_BUDGET_DELETE',
                                                   'COUNCIL_TRANSACTION_VIEW', 'COUNCIL_TRANSACTION_CREATE', 'COUNCIL_TRANSACTION_EDIT', 'COUNCIL_TRANSACTION_DELETE',
                                                   'EVENT_VIEW', 'EVENT_VIEW_DRAFTS', 'EVENT_APPROVE', 'EVENT_DELETE','EVENT_REMOVE_PARTICIPANTS',
                                                   'SUGGESTION_VIEW', 'SUGGESTION_APPROVE', 'SUGGESTION_REJECT',
                                                   'REPORT_VIEW', 'REPORT_GENERATE'
    )
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_code IN ('PRZEWODNICZACY_SU', 'ZASTEPCA_SU') AND p.name IN (
                                                                         'USER_VIEW',
                                                                         'COUNCIL_VIEW', 'COUNCIL_MEMBER_MANAGE', 'COUNCIL_JOIN', 'COUNCIL_EDIT',
                                                                         'COUNCIL_BUDGET_VIEW', 'COUNCIL_BUDGET_CREATE', 'COUNCIL_BUDGET_EDIT',
                                                                         'COUNCIL_TRANSACTION_VIEW', 'COUNCIL_TRANSACTION_CREATE', 'COUNCIL_TRANSACTION_EDIT', 'COUNCIL_TRANSACTION_DELETE',
                                                                         'EVENT_VIEW', 'EVENT_CREATE', 'EVENT_EDIT', 'EVENT_APPROVE', 'EVENT_VIEW_DRAFTS','EVENT_REMOVE_PARTICIPANTS',
                                                                         'SUGGESTION_VIEW', 'SUGGESTION_APPROVE', 'SUGGESTION_REJECT',
                                                                         'REPORT_VIEW', 'REPORT_GENERATE'
    )
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_code = 'SKARBNIK_SU' AND p.name IN (
                                                 'USER_VIEW',
                                                 'COUNCIL_VIEW', 'COUNCIL_MEMBER_MANAGE', 'COUNCIL_JOIN',
                                                 'COUNCIL_BUDGET_VIEW', 'COUNCIL_BUDGET_CREATE',
                                                 'COUNCIL_TRANSACTION_VIEW', 'COUNCIL_TRANSACTION_CREATE', 'COUNCIL_TRANSACTION_EDIT',
                                                 'EVENT_VIEW', 'EVENT_VIEW_DRAFTS',
                                                 'SUGGESTION_VIEW',
                                                 'REPORT_VIEW', 'REPORT_GENERATE'
    )
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_code = 'CZLONEK_SU' AND p.name IN (
                                                'USER_VIEW', 'COUNCIL_VIEW', 'COUNCIL_JOIN',
                                                'COUNCIL_BUDGET_VIEW', 'COUNCIL_TRANSACTION_VIEW',
                                                'EVENT_VIEW', 'EVENT_CREATE', 'EVENT_VIEW_DRAFTS',
                                                'SUGGESTION_VIEW', 'REPORT_VIEW'
    )
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_code = 'BYLY_CZLONEK_SU' AND p.name IN (
                                                'USER_VIEW', 'COUNCIL_VIEW', 'COUNCIL_JOIN',
                                                'EVENT_VIEW', 'EVENT_CREATE', 'EVENT_VIEW_DRAFTS',
                                                'SUGGESTION_VIEW'

    )
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_code = 'UCZEN' AND p.name IN ('EVENT_VIEW', 'SUGGESTION_CREATE', 'COUNCIL_JOIN', 'COUNCIL_VIEW', 'USER_VIEW')
    ON CONFLICT DO NOTHING;