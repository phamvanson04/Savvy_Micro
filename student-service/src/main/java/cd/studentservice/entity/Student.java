package cd.studentservice.entity;

import cd.studentservice.enumerate.StudentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "students")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE students SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at is null")
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(unique = true,nullable = false)
    String code;

    String fullName;
    LocalDate dob;
    Boolean gender;
    @Enumerated(EnumType.STRING)
    StudentStatus status;
    @CreationTimestamp
    Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "class_id")
    Class clazz;
}
