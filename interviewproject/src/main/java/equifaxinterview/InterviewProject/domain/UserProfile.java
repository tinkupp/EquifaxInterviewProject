package equifaxinterview.InterviewProject.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    String id; // data store id

    String username;

    String email;

    String socialSecurityNumber;
}
