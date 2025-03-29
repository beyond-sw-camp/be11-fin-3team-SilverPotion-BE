package silverpotion.gateway;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter {
 //GlobalFilter는 Spring Cloud Gateway의 모든 요청을 가로채는 필터 인터페이스
 //이 필터가 있기때문에 필터가 생기는 security의존성을 게이트웨이의 build.gradle에 추가하지 않아도 됨.
    @Value("${jwt.secretKey}")
    private String secretKey;

    //    인증이 필요 없는 경로 설정 아래쪽 코드를 보면 여기 리스트에 있는 경로에 대해서는 토큰을 꺼내 검증하는 로직에서 패스시킴
    private static final List<String> ALLOWED_PATHS = List.of(
            "/silverpotion/user/create",
            "/silverpotion/user/login"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    //ServerWebExchange : 요청과 응답을 담고 있는 WebFlux 의 객체(HttpServletRequest/Response같은 역할), GatewayFilterChain: 다음 필터 또는 서비스로 요청을 넘기는 체인
    //우리가 지금껏 해오던건 Spring MVC방식, 게이트웨이는 WebFlux방식으로 작동함
    //Mono<void>= 아무 값도 없이 완료 신호만 보내는 비동기 응답  ex.Mono.error =예외 발생 , Mono.empty = 작업끝났음
        //token검증
        System.out.println("token 검증 시작");
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String path = exchange.getRequest().getPath().toString();
        if (ALLOWED_PATHS.contains(path)) {
            return chain.filter(exchange); //현재 요청을 담고 있는 exchange를 들고 다음 필터나 서비스로 넘어가라는 뜻
        }

        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("token 관련 예외 발생");
            }
            String token = bearerToken.substring(7);

            //게이트웨이인 이곳에서 token 검증하고 token에 있는 claim 추출
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            //claim에 있는 사용자 Id추출
            String userId = claims.getSubject(); //우리가 tokenProvider에서 claim의subject에 loginId를 담았었으니까 아이디 추출가능
            String role = claims.get("role", String.class);
            //헤더에 X-User-Id변수로 id값과 role을 추가
            //X를 붙이는 것은 custom header라는 것을 의미하는 것으로 널리 쓰이는 관례
            ServerWebExchange modifiedExchange = exchange.mutate() // 사용자의 요청이 담겨있는 exchange의 헤더 부분을 커스텀하고 있음
                    .request(builder -> builder
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "ROLE_" + role))
                    .build();
            //ServerWebExchange는 불변객체로  사실 수정할 수 없다.그래서 복사본을 만드는 메서드 mutate(). 그런데 이 메서드의 리턴 타입이
            //ServerWebExchange.Builder이고 .request는 요청(request)부분을 수정하겠다는 뜻
            //다시 filter chain으로 되돌아 가는 로직
            return chain.filter(modifiedExchange); //우리가 커스텀한 요청인 modifiedExchange를 들고 다음 필터나 서비스로 리턴
        } catch (IllegalArgumentException | MalformedJwtException | ExpiredJwtException | SignatureException |
                 UnsupportedJwtException e) {
            e.printStackTrace();
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
