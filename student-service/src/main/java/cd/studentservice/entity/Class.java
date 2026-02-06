package cd.studentservice.entity;

import cd.studentservice.enumerate.ClassStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "classes")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE classes SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at is null")
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(unique = true,nullable = false)
    String code;
    String name;
    Integer grade;
    @Enumerated(EnumType.STRING)
    ClassStatus status;

    @CreationTimestamp
    Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "school_id")
    School school;
}
