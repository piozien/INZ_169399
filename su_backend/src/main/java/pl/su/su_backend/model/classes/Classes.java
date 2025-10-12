package pl.su.su_backend.model.classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "classes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Classes {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column
	private String year;
}
