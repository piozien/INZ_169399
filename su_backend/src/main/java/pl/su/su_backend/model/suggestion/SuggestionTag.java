package pl.su.su_backend.model.suggestion;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "suggestion_tags")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionTag {

	@Embeddable
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode
	public static class Id implements Serializable {
		private java.util.UUID suggestionId;
		private String tag;
	}

	@EmbeddedId
	private Id id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("suggestionId")
	@JoinColumn(name = "suggestion_id")
	private Suggestion suggestion;
}
