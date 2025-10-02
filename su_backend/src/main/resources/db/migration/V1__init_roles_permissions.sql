-- Roles table (stores enum code and optional description)
CREATE TABLE IF NOT EXISTS roles (
	id UUID PRIMARY KEY,
	role_code VARCHAR(50) UNIQUE NOT NULL,
	description TEXT
);

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
	id UUID PRIMARY KEY,
	code VARCHAR(100) UNIQUE NOT NULL,
	description VARCHAR(255)
);

-- Role-Permissions join table (composite PK)
CREATE TABLE IF NOT EXISTS role_permissions (
	role_id UUID NOT NULL,
	permission_id UUID NOT NULL,
	PRIMARY KEY (role_id, permission_id),
	CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
	CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_role_permissions_permission ON role_permissions(permission_id);

-- Users table assumed existing; creating join table for User-Role
CREATE TABLE IF NOT EXISTS user_roles (
	user_id UUID NOT NULL,
	role_id UUID NOT NULL,
	assigned_at TIMESTAMP NOT NULL,
	PRIMARY KEY (user_id, role_id),
	CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
	CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role_id);
