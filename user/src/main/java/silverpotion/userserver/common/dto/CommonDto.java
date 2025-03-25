package silverpotion.userserver.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommonDto {
    private int status_code;  //상태코드
    private String status_message;  //상태메시지
    private Object result;  //데이터
}
