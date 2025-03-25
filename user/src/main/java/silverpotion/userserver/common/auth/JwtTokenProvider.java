//package silverpotion.user.common.auth;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//import javax.xml.crypto.Data;
//import java.security.Key;
//import java.security.Signature;
//import java.util.Date;
//
//@Component
//public class JwtTokenProvider {
//
//    @Value("${jwt.expiration}")
//    private int expriration;
//    @Value("${jwt.secretKey}")
//    private String  secretKey;
//
//    @Value("${jwt.expirationRt}")
//    private int exprirationRt;
//    @Value("${jwt.secretKeyRt}")
//    private String  secretKeyRt;
//
//    private Key ENCRYPTED_SECRET_KEY;
//    private Key ENCRYPTED_RT_SECRET_KEY;
//
////  생성자가 호출되고, 스프링빈이 만들어진 직후에 아래 메서드 바로 실행하는 어노테이션
//    @PostConstruct
//    public void init(){
//        ENCRYPTED_SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
//        ENCRYPTED_RT_SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyRt), SignatureAlgorithm.HS512.getJcaName());
//    }
//
//
//    public String createToken(String email, String role){
//        Claims claims = Jwts.claims().setSubject(email);
//        claims.put("role",role);
//        Date now = new Date();
//        String token = Jwts.builder()
//                //Claims는 사용자정보(페이로드 정보)
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(new Date(now.getTime()+expriration*60*1000L))//30분 세팅
//                .signWith(ENCRYPTED_SECRET_KEY)
//                .compact();
//
//        return token;
//
//    }
//
//    public String createRefreshToken(String email, String role){
//        Claims claims = Jwts.claims().setSubject(email);
//        claims.put("role",role);
//        Date now = new Date();
//        String token = Jwts.builder()
//                //Claims는 사용자정보(페이로드 정보)
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(new Date(now.getTime()+exprirationRt*60*1000L))//30분 세팅
//                .signWith(ENCRYPTED_RT_SECRET_KEY)
//                .compact();
//
//        return token;
//
//    }
//}
