-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       full_name VARCHAR(255),
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255),
                       status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                       auth_provider VARCHAR(50) NOT NULL,
                       external_id VARCHAR(255),
                       refresh_token TEXT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       role_code VARCHAR(100) UNIQUE NOT NULL,
                       description VARCHAR(255)
);

CREATE TABLE permissions (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             name VARCHAR(100) UNIQUE NOT NULL,
                             description VARCHAR(255)
);

CREATE TABLE role_permissions (
                                  role_id UUID NOT NULL,
                                  permission_id UUID NOT NULL,
                                  PRIMARY KEY (role_id, permission_id),
                                  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
                                  FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id UUID NOT NULL,
                            assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE councils (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          name VARCHAR(255) NOT NULL,
                          academic_year VARCHAR(20) NOT NULL,
                          start_date DATE NOT NULL,
                          end_date DATE NOT NULL,
                          is_active BOOLEAN NOT NULL DEFAULT true,
                          is_default BOOLEAN NOT NULL DEFAULT false,
                          join_code VARCHAR(36) UNIQUE NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          UNIQUE(name, academic_year)
);

CREATE TABLE council_members (
                                 council_id UUID NOT NULL,
                                 user_id UUID NOT NULL,
                                 role VARCHAR(50) NOT NULL,
                                 PRIMARY KEY (council_id, user_id),
                                 FOREIGN KEY (council_id) REFERENCES councils(id) ON DELETE CASCADE,
                                 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE council_budgets (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 council_id UUID NOT NULL,
                                 year VARCHAR(10) NOT NULL,
                                 initial_amount DECIMAL(12,2),
                                 balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                                 created_by UUID NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (council_id) REFERENCES councils(id) ON DELETE CASCADE,
                                 FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
                                 UNIQUE(council_id, year)
);

CREATE TABLE council_transactions (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      budget_id UUID NOT NULL,
                                      type VARCHAR(50) NOT NULL,
                                      amount DECIMAL(12,2) NOT NULL,
                                      description TEXT NOT NULL,
                                      date TIMESTAMP NOT NULL,
                                      added_by UUID NOT NULL,
                                      FOREIGN KEY (budget_id) REFERENCES council_budgets(id) ON DELETE CASCADE,
                                      FOREIGN KEY (added_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE events (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        title VARCHAR(255) NOT NULL,
                        description TEXT NOT NULL,
                        start_date TIMESTAMP NOT NULL,
                        end_date TIMESTAMP NOT NULL,
                        location VARCHAR(255),
                        max_participants INTEGER,
                        participants_count INTEGER NOT NULL DEFAULT 0,
                        status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
                        created_by UUID NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        council_id UUID,
                        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY (council_id) REFERENCES councils(id) ON DELETE CASCADE
);

CREATE TABLE event_participants (
                                    event_id UUID NOT NULL,
                                    user_id UUID NOT NULL,
                                    role VARCHAR(50) NOT NULL DEFAULT 'PARTICIPANT',
                                    confirmed BOOLEAN NOT NULL DEFAULT FALSE,
                                    assigned_at TIMESTAMP NOT NULL,
                                    PRIMARY KEY (event_id, user_id),
                                    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
                                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE suggestions (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             user_id UUID NOT NULL,
                             title VARCHAR(255) NOT NULL,
                             description TEXT NOT NULL,
                             status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                             is_anonymous BOOLEAN DEFAULT FALSE,
                             rejection_reason TEXT,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             council_id UUID NOT NULL,
                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                             FOREIGN KEY (council_id) REFERENCES councils(id) ON DELETE CASCADE
);

CREATE TABLE suggestion_tags (
                                 suggestion_id UUID NOT NULL,
                                 tag VARCHAR(100) NOT NULL,
                                 PRIMARY KEY (suggestion_id, tag),
                                 FOREIGN KEY (suggestion_id) REFERENCES suggestions(id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       user_id UUID NOT NULL,
                                       token VARCHAR(255) UNIQUE NOT NULL,
                                       expires_at TIMESTAMP NOT NULL,
                                       is_used BOOLEAN DEFAULT FALSE,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE activity_logs (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id UUID NOT NULL,
                               action_type VARCHAR(100) NOT NULL,
                               action TEXT NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);