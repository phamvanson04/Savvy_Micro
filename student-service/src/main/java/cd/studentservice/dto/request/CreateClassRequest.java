package cd.studentservice.dto.request;

import cd.studentservice.enumerate.ClassStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateClassRequest {
    String code;
    String name;
    Integer grade;
    ClassStatus status;
    UUID schoolId;
}
