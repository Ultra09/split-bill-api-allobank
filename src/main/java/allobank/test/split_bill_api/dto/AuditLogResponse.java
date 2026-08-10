package allobank.test.split_bill_api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogResponse {
    private Long id;
    private String action;
    private String description;
    private LocalDateTime timestamp;
}
