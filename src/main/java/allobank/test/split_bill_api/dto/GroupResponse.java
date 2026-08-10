package allobank.test.split_bill_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class GroupResponse {
    private Long id;
    private String name;
    private List<ParticipantDto> participants;

    @Data
    public static class ParticipantDto {
        private Long id;
        private String name;
    }
}
