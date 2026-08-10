package allobank.test.split_bill_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GroupCreateRequest {
    @NotBlank(message = "Group name is required")
    private String name;

    @NotEmpty(message = "At least one participant is required")
    private List<@NotBlank(message = "Participant name cannot be blank") String> participants;
}
