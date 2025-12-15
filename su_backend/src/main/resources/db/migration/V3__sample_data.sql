INSERT INTO users (full_name, email, password, status, auth_provider, created_at) VALUES
    ('Administrator Systemu', 'admin@school.edu', '$2a$10$gxrCRYmrJvnWSZ095SP2dOt86BAnCq822W0kN0ANj2NhgrNQbXEAi', 'CONFIRMED', 'LOCAL', NOW())
    ON CONFLICT (email) DO NOTHING;
-- dyrektor
INSERT INTO users (full_name, email, password, status, auth_provider, created_at) VALUES
    ('DYREKTOR', 'dyrektor@school.edu', '$2a$10$gxrCRYmrJvnWSZ095SP2dOt86BAnCq822W0kN0ANj2NhgrNQbXEAi', 'CONFIRMED', 'LOCAL', NOW())
    ON CONFLICT (email) DO NOTHING;
-- zastepca dyrektora
INSERT INTO users (full_name, email, password, status, auth_provider, created_at) VALUES
    ('ZASTEPCA_DYREKTORA', 'zastepca@school.edu', '$2a$10$gxrCRYmrJvnWSZ095SP2dOt86BAnCq822W0kN0ANj2NhgrNQbXEAi', 'CONFIRMED', 'LOCAL', NOW())
    ON CONFLICT (email) DO NOTHING;
-- opiekun
INSERT INTO users (full_name, email, password, status, auth_provider, created_at) VALUES
    ('Nauczyciel', 'nauczyciel@school.edu', '$2a$10$gxrCRYmrJvnWSZ095SP2dOt86BAnCq822W0kN0ANj2NhgrNQbXEAi', 'CONFIRMED', 'LOCAL', NOW())
    ON CONFLICT (email) DO NOTHING;

-- przewodniczacy
INSERT INTO users (full_name, email, password, status, auth_provider, created_at) VALUES
    ('Uczen', 'student1@school.edu', '$2a$10$gxrCRYmrJvnWSZ095SP2dOt86BAnCq822W0kN0ANj2NhgrNQbXEAi', 'CONFIRMED', 'LOCAL', NOW())
    ON CONFLICT (email) DO NOTHING;
-- zastepca
INSERT INTO users (full_name, email, password, status, auth_provider, created_at) VALUES
    ('Uczen', 'student2@school.edu', '$2a$10$gxrCRYmrJvnWSZ095SP2dOt86BAnCq822W0kN0ANj2NhgrNQbXEAi', 'CONFIRMED', 'LOCAL', NOW())
    ON CONFLICT (email) DO NOTHING;
-- skarbnik
INSERT INTO users (full_name, email, password, status, auth_provider, created_at) VALUES
    ('Uczen', 'student3@school.edu', '$2a$10$gxrCRYmrJvnWSZ095SP2dOt86BAnCq822W0kN0ANj2NhgrNQbXEAi', 'CONFIRMED', 'LOCAL', NOW())
    ON CONFLICT (email) DO NOTHING;
-- czlonek su
INSERT INTO users (full_name, email, password, status, auth_provider, created_at) VALUES
    ('Uczen', 'student4@school.edu', '$2a$10$gxrCRYmrJvnWSZ095SP2dOt86BAnCq822W0kN0ANj2NhgrNQbXEAi', 'CONFIRMED', 'LOCAL', NOW())
    ON CONFLICT (email) DO NOTHING;
-- czlonek su
INSERT INTO users (full_name, email, password, status, auth_provider, created_at) VALUES
    ('Uczen', 'student5@school.edu', '$2a$10$gxrCRYmrJvnWSZ095SP2dOt86BAnCq822W0kN0ANj2NhgrNQbXEAi', 'CONFIRMED', 'LOCAL', NOW())
    ON CONFLICT (email) DO NOTHING;
-- uczen bez su
INSERT INTO users (full_name, email, password, status, auth_provider, created_at) VALUES
    ('Uczen', 'student6@school.edu', '$2a$10$gxrCRYmrJvnWSZ095SP2dOt86BAnCq822W0kN0ANj2NhgrNQbXEAi', 'CONFIRMED', 'LOCAL', NOW())
    ON CONFLICT (email) DO NOTHING;
-- zablokowany
INSERT INTO users (full_name, email, password, status, auth_provider, created_at) VALUES
    ('Uczen', 'student7@school.edu', '$2a$10$gxrCRYmrJvnWSZ095SP2dOt86BAnCq822W0kN0ANj2NhgrNQbXEAi', 'BLOCKED', 'LOCAL', NOW())
    ON CONFLICT (email) DO NOTHING;


INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@school.edu' AND r.role_code = 'ADMINISTRATOR'
    ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'dyrektor@school.edu' AND r.role_code = 'DYREKTOR'
    ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'zastepca@school.edu' AND r.role_code = 'ZASTEPCA_DYREKTORA'
    ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'nauczyciel@school.edu' AND r.role_code = 'NAUCZYCIEL'
    ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email LIKE 'student%' AND r.role_code = 'UCZEN'
    ON CONFLICT DO NOTHING;


INSERT INTO councils (name, academic_year, start_date, end_date, is_active, is_default,  join_code, created_at) VALUES
    ('Samorząd Uczniowski 2021/2022', '2021/2022', '2021-09-01', '2022-06-30', false, false, 'OLD2021', '2021-09-01 08:00:00')
    ON CONFLICT (name, academic_year) DO NOTHING;

INSERT INTO councils (name, academic_year, start_date, end_date, is_active, is_default, join_code, created_at) VALUES
    ('Samorząd Uczniowski 2022/2023', '2022/2023', '2022-09-01', '2023-06-30', false, false, 'OLD2022', '2022-09-01 08:00:00')
    ON CONFLICT (name, academic_year) DO NOTHING;

INSERT INTO councils (name, academic_year, start_date, end_date, is_active, is_default, join_code, created_at) VALUES
    ('Samorząd Uczniowski 2023/2024', '2023/2024', '2023-09-01', '2024-06-30', false, false,  'OLD2023', '2023-09-01 08:00:00')
    ON CONFLICT (name, academic_year) DO NOTHING;


INSERT INTO councils (name, academic_year, start_date, end_date, is_active, is_default, join_code, created_at) VALUES
    ('Samorząd Uczniowski 2025/2026', '2025/2026', '2025-09-01', '2026-06-30', true,true, 'SU2025', '2025-09-01 08:00:00')
    ON CONFLICT (name, academic_year) DO NOTHING;

INSERT INTO council_members (council_id, user_id, role)
SELECT c.id, u.id, 'OPIEKUN_SU' FROM councils c, users u
WHERE c.join_code = 'SU2025' AND u.email = 'nauczyciel@school.edu';

INSERT INTO council_members (council_id, user_id, role)
SELECT c.id, u.id, 'PRZEWODNICZACY_SU' FROM councils c, users u
WHERE c.join_code = 'SU2025' AND u.email = 'student1@school.edu';

INSERT INTO council_members (council_id, user_id, role)
SELECT c.id, u.id, 'ZASTEPCA_SU' FROM councils c, users u
WHERE c.join_code = 'SU2025' AND u.email = 'student2@school.edu';

INSERT INTO council_members (council_id, user_id, role)
SELECT c.id, u.id, 'SKARBNIK_SU' FROM councils c, users u
WHERE c.join_code = 'SU2025' AND u.email = 'student3@school.edu';

INSERT INTO council_members (council_id, user_id, role)
SELECT c.id, u.id, 'OPIEKUN_SU' FROM councils c, users u
WHERE c.join_code = 'SU2025' AND u.email = 'opiekun@school.edu';

INSERT INTO council_members (council_id, user_id, role)
SELECT c.id, u.id, 'CZLONEK_SU' FROM councils c, users u
WHERE c.join_code = 'SU2025' AND u.email IN
                                 ('student4@school.edu', 'student5@school.edu');


INSERT INTO council_budgets (id, council_id, year, initial_amount, balance, created_by, created_at)
SELECT gen_random_uuid(), id, '2023/2024', 1000.00, 50.00, (SELECT id FROM users WHERE email = 'admin@school.edu'), NOW()
FROM councils WHERE join_code = 'OLD2023'
    ON CONFLICT DO NOTHING;

INSERT INTO council_budgets (id, council_id, year, initial_amount, balance, created_by, created_at)
SELECT
    gen_random_uuid(),
    c.id,
    '2025/2026',
    2000.00,
    2450.00,
    (SELECT id FROM users WHERE email = 'admin@school.edu'),
    '2025-09-01 10:00:00'
FROM councils c WHERE c.join_code = 'SU2025'
    ON CONFLICT DO NOTHING;

INSERT INTO council_transactions (id, budget_id, type, amount, description, date, added_by)
SELECT gen_random_uuid(), cb.id, 'INCOME', 500.00, 'Jesienny Kiermasz Ciast', '2025-10-15 12:30:00', (SELECT id FROM users WHERE email = 'student3@school.edu')
FROM council_budgets cb JOIN councils c ON cb.council_id = c.id WHERE c.join_code = 'SU2025';

INSERT INTO council_transactions (id, budget_id, type, amount, description, date, added_by)
SELECT gen_random_uuid(), cb.id, 'EXPENSE', 150.00, 'Papier ksero i markery', '2025-10-20 14:00:00', (SELECT id FROM users WHERE email = 'student3@school.edu')
FROM council_budgets cb JOIN councils c ON cb.council_id = c.id WHERE c.join_code = 'SU2025';

INSERT INTO council_transactions (id, budget_id, type, amount, description, date, added_by)
SELECT gen_random_uuid(), cb.id, 'INCOME', 300.00, 'Bilety na Dyskotekę Andrzejkową', '2025-11-28 20:00:00', (SELECT id FROM users WHERE email = 'student3@school.edu')
FROM council_budgets cb JOIN councils c ON cb.council_id = c.id WHERE c.join_code = 'SU2025';

INSERT INTO council_transactions (id, budget_id, type, amount, description, date, added_by)
SELECT gen_random_uuid(), cb.id, 'EXPENSE', 200.00, 'Naprawa kolumny głośnikowej', '2025-12-01 09:00:00', (SELECT id FROM users WHERE email = 'student3@school.edu')
FROM council_budgets cb JOIN councils c ON cb.council_id = c.id WHERE c.join_code = 'SU2025';


INSERT INTO events (id, council_id, title, description, start_date, end_date, location, status, created_by, max_participants, participants_count)
SELECT gen_random_uuid(), c.id, 'Uroczyste Rozpoczęcie Roku', 'Apel na sali gimnastycznej dla klas 4-8.', '2025-09-01 09:00:00', '2025-09-01 11:00:00', 'Sala Gimnastyczna', 'APPROVED', (SELECT id FROM users WHERE email = 'admin@school.edu'),
       NULL, 0
FROM councils c WHERE c.join_code = 'SU2025';

INSERT INTO events (id, council_id, title, description, start_date, end_date, location, status, created_by, max_participants, participants_count)
SELECT gen_random_uuid(), c.id, 'Dzień Nauczyciela', 'Rozdanie kwiatów i krótka akademia.', '2025-10-14 11:00:00', '2025-10-14 13:00:00', 'Cała szkoła', 'APPROVED', (SELECT id FROM users WHERE email = 'student1@school.edu'),
       NULL, 0
FROM councils c WHERE c.join_code = 'SU2025';

INSERT INTO events (id, council_id, title, description, start_date, end_date, location, status, created_by, max_participants, participants_count)
SELECT gen_random_uuid(), c.id, 'Dyskoteka Andrzejkowa', 'Zabawa taneczna, wróżby i konkursy.', '2025-11-28 17:00:00', '2025-11-28 21:00:00', 'Korytarz Główny', 'APPROVED', (SELECT id FROM users WHERE email = 'student2@school.edu'),
       NULL, 0
FROM councils c WHERE c.join_code = 'SU2025';

INSERT INTO events (id, council_id, title, description, start_date, end_date, location, status, created_by, max_participants, participants_count)
SELECT gen_random_uuid(), c.id, 'Szkolna Wigilia', 'Wspólne kolędowanie i opłatek.', '2025-12-20 10:00:00', '2025-12-20 13:00:00', 'Aula', 'APPROVED', (SELECT id FROM users WHERE email = 'student1@school.edu'),
       NULL, 0
FROM councils c WHERE c.join_code = 'SU2025';

INSERT INTO events (id, council_id, title, description, start_date, end_date, location, status, created_by, max_participants, participants_count)
SELECT gen_random_uuid(), c.id, 'Kiermasz książek', 'Chcesz się wymienić książkami? Nabyć nowe? Zapraszamy na kiermasz!', '2026-03-02 08:00:00', '2026-03-06 14:00:00', 'Cała szkoła', 'APPROVED', (SELECT id FROM users WHERE email = 'student1@school.edu'),
       NULL, 0
FROM councils c WHERE c.join_code = 'SU2025';

INSERT INTO events (id, council_id, title, description, start_date, end_date, location, status, created_by, max_participants, participants_count)
SELECT gen_random_uuid(), c.id, 'Poczta Walentynkowa', 'Zbiórka kartek i rozdawanie.', '2026-02-14 08:00:00', '2026-02-14 15:00:00', 'Hol', 'PENDING', (SELECT id FROM users WHERE email = 'student4@school.edu'),
       NULL, 0
FROM councils c WHERE c.join_code = 'SU2025';

INSERT INTO events (id, council_id, title, description, start_date, end_date, location, status, created_by, max_participants, participants_count)
SELECT gen_random_uuid(), c.id, 'Turniej E-Sportowy FIFA', 'Wstępny plan turnieju na konsolach.', '2026-01-15 16:00:00', '2026-01-15 20:00:00', 'Sala Informatyczna', 'DRAFT', (SELECT id FROM users WHERE email = 'student3@school.edu'),
       32, 0
FROM councils c WHERE c.join_code = 'SU2025';

INSERT INTO events (id, council_id, title, description, start_date, end_date, location, status, created_by, max_participants, participants_count)
SELECT gen_random_uuid(), c.id, 'Noc Filmowa', 'Spanie w szkole', '2025-11-10 20:00:00', '2025-11-11 08:00:00', 'Sala 12', 'REJECTED', (SELECT id FROM users WHERE email = 'student5@school.edu'),
       30, 0
FROM councils c WHERE c.join_code = 'SU2025';

INSERT INTO event_participants (event_id, user_id, assigned_at, role, confirmed)
SELECT
    e.id,
    u.id,
    NOW(),
    'PARTICIPANT',
    FALSE
FROM users u
         CROSS JOIN events e
WHERE e.title = 'Szkolna Wigilia'
    ON CONFLICT (event_id, user_id) DO NOTHING;

UPDATE events
SET participants_count = (
    SELECT COUNT(*)
    FROM event_participants
    WHERE event_id = events.id
)
WHERE title = 'Szkolna Wigilia';