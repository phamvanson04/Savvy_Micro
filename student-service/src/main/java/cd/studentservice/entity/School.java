package cd.studentservice.entity;

import cd.studentservice.enumerate.SchoolStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "schools")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE schools SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at is null")
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(unique = true,nullable = false)
    String code;
    @Column(nullable = false)
    String name;
    String address;
    @Enumerated(EnumType.STRING)
    SchoolStatus status;
    Instant createdAt;
    Instant deletedAt;
}
