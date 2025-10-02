-- Classes
CREATE TABLE IF NOT EXISTS classes (
	id UUID PRIMARY KEY,
	name VARCHAR(50) NOT NULL,
	year VARCHAR(20)
);

-- Councils (needed before council_budgets FK)
CREATE TABLE IF NOT EXISTS councils (
	id UUID PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	created_at TIMESTAMP NOT NULL
);

-- Class budgets
CREATE TABLE IF NOT EXISTS class_budgets (
	id UUID PRIMARY KEY,
	class_id UUID NOT NULL,
	year INTEGER,
	created_by UUID NOT NULL,
	created_at TIMESTAMP NOT NULL,
	CONSTRAINT fk_class_budgets_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE RESTRICT,
	CONSTRAINT fk_class_budgets_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_class_budgets_class ON class_budgets(class_id);

-- Council budgets
CREATE TABLE IF NOT EXISTS council_budgets (
	id UUID PRIMARY KEY,
	council_id UUID NOT NULL,
	year INTEGER,
	created_by UUID NOT NULL,
	created_at TIMESTAMP NOT NULL,
	CONSTRAINT fk_council_budgets_council FOREIGN KEY (council_id) REFERENCES councils(id) ON DELETE RESTRICT,
	CONSTRAINT fk_council_budgets_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_council_budgets_council ON council_budgets(council_id);

-- Class transactions
CREATE TABLE IF NOT EXISTS class_transactions (
	id UUID PRIMARY KEY,
	budget_id UUID NOT NULL,
	type VARCHAR(20) NOT NULL,
	amount DECIMAL(12,2) NOT NULL,
	description VARCHAR(255) NOT NULL,
	date TIMESTAMP NOT NULL,
	added_by UUID NOT NULL,
	payer_user UUID,
	confirmed BOOLEAN NOT NULL DEFAULT FALSE,
	CONSTRAINT fk_class_tx_budget FOREIGN KEY (budget_id) REFERENCES class_budgets(id) ON DELETE CASCADE,
	CONSTRAINT fk_class_tx_added_by FOREIGN KEY (added_by) REFERENCES users(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_class_tx_budget ON class_transactions(budget_id);
CREATE INDEX IF NOT EXISTS idx_class_tx_added_by ON class_transactions(added_by);

-- Council transactions
CREATE TABLE IF NOT EXISTS council_transactions (
	id UUID PRIMARY KEY,
	budget_id UUID NOT NULL,
	type VARCHAR(20) NOT NULL,
	amount DECIMAL(12,2) NOT NULL,
	description VARCHAR(255) NOT NULL,
	date TIMESTAMP NOT NULL,
	added_by UUID NOT NULL,
	confirmed BOOLEAN NOT NULL DEFAULT FALSE,
	CONSTRAINT fk_council_tx_budget FOREIGN KEY (budget_id) REFERENCES council_budgets(id) ON DELETE CASCADE,
	CONSTRAINT fk_council_tx_added_by FOREIGN KEY (added_by) REFERENCES users(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_council_tx_budget ON council_transactions(budget_id);
CREATE INDEX IF NOT EXISTS idx_council_tx_added_by ON council_transactions(added_by);
