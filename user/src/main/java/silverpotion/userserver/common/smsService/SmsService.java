package silverpotion.userserver.common.smsService;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SmsService {

    private final StringRedisTemplate redisTemplate;

    public SmsService(@Qualifier("stringRedisTemplateforSms")StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = stringRedisTemplate;
    }

    // 변수 주입.
    @Value("${coolsms.apiKey}")
    private String apiKey;
    @Value("${coolsms.apiSecret}")
    private String apiSecret;
    @Value("${coolsms.fromNumber}")
    private String fromNumber;

    // 랜덤 4자리 인증번호 생성
    private String generateAuthCode() {
        Random rand = new Random();
        int code = 1000 + rand.nextInt(9000); // 1000 ~ 9999
        return String.valueOf(code);
    }

    public String sendAuthCode(String phoneNumber){
        DefaultMessageService messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");;
        String authCode = generateAuthCode();
        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(phoneNumber);
        message.setText("[인증번호] " + authCode + " (5분 내 입력)");

        try {
            messageService.send(message);
            //보낸 코드 유효시간 5분 설정
            redisTemplate.opsForValue().set(phoneNumber, authCode, 5, TimeUnit.MINUTES);
            return "인증번호 전송 완료!";
        } catch (Exception e) {
            e.printStackTrace();
            return "SMS 전송 실패: " + e.getMessage();
        }
    }

    // 인증번호 검증
    public boolean verifyAuthCode(String phoneNumber, String inputCode) {
        //Redis에서 해당 phoneNumber 키에 저장된 인증번호를 가져온다.
        String savedCode = redisTemplate.opsForValue().get(phoneNumber);
        System.out.println("savedCode : " + savedCode);
        return savedCode != null && savedCode.equals(inputCode);
    }
}
