package backend.back.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ClientResponse {
    private Long id;
    private String lastName;
    private String firstName;
    private String patronymic;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createdAt;
    private List<CalculationShortResponse> calculations;

    @Data
    public static class CalculationShortResponse {
        private Long id;
        private String constructionAddress;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime pricesFixedUntil;
    }
}