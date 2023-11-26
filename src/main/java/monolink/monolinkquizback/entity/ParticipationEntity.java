package monolink.monolinkquizback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "responses")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationEntity {

    @Id
    private UUID id;

    @OneToOne
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json", name = "responses")
    private List<Response> responses;

}
