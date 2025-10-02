-- Events
CREATE TABLE IF NOT EXISTS events (
	id UUID PRIMARY KEY,
	title VARCHAR(255) NOT NULL,
	description TEXT NOT NULL,
	start_date TIMESTAMP NOT NULL,
	end_date TIMESTAMP NOT NULL,
	location VARCHAR(255),
	created_by UUID NOT NULL,
	calendar_event_id VARCHAR(255),
	created_at TIMESTAMP NOT NULL,
	CONSTRAINT fk_events_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_events_created_by ON events(created_by);
CREATE INDEX IF NOT EXISTS idx_events_start_date ON events(start_date);

-- Event participants
CREATE TABLE IF NOT EXISTS event_participants (
	event_id UUID NOT NULL,
	user_id UUID NOT NULL,
	role VARCHAR(20) NOT NULL,
	confirmed BOOLEAN NOT NULL DEFAULT FALSE,
	assigned_at TIMESTAMP NOT NULL,
	PRIMARY KEY (event_id, user_id),
	CONSTRAINT fk_ep_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
	CONSTRAINT fk_ep_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_ep_user ON event_participants(user_id);

-- Suggestions
CREATE TABLE IF NOT EXISTS suggestions (
	id UUID PRIMARY KEY,
	user_id UUID NOT NULL,
	title VARCHAR(255) NOT NULL,
	description TEXT NOT NULL,
	is_anonymous BOOLEAN NOT NULL,
	status VARCHAR(20) NOT NULL,
	created_at TIMESTAMP NOT NULL,
	CONSTRAINT fk_suggestions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_suggestions_user ON suggestions(user_id);
CREATE INDEX IF NOT EXISTS idx_suggestions_status ON suggestions(status);

-- Suggestion tags
CREATE TABLE IF NOT EXISTS suggestion_tags (
	suggestion_id UUID NOT NULL,
	tag VARCHAR(64) NOT NULL,
	PRIMARY KEY (suggestion_id, tag),
	CONSTRAINT fk_suggestion_tags_suggestion FOREIGN KEY (suggestion_id) REFERENCES suggestions(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_suggestion_tags_tag ON suggestion_tags(tag);

-- Comments
CREATE TABLE IF NOT EXISTS comments (
	id UUID PRIMARY KEY,
	event_id UUID NOT NULL,
	user_id UUID NOT NULL,
	content TEXT NOT NULL,
	rating INTEGER,
	created_at TIMESTAMP NOT NULL,
	CONSTRAINT fk_comments_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
	CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_comments_event ON comments(event_id);
CREATE INDEX IF NOT EXISTS idx_comments_user ON comments(user_id);

-- Activity logs
CREATE TABLE IF NOT EXISTS activity_logs (
	id UUID PRIMARY KEY,
	user_id UUID NOT NULL,
	action_type VARCHAR(50) NOT NULL,
	action TEXT NOT NULL,
	created_at TIMESTAMP NOT NULL,
	CONSTRAINT fk_activity_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_activity_logs_user ON activity_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_created_at ON activity_logs(created_at);
