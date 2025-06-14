package silverpotion.userserver.openAi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HealthReportDto {

    private String text;
    private LocalDate date;
    private String period;
    private String imgUrl;

}
