//package silverpotion.user.common.auth;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.AuthenticationServiceException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class JwtAuthFilter extends GenericFilter {
//    @Value("${jwt.secretKey}")
//    private String secretKey;
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
////        token 검증
//        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
//        HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;
//        String bearerToken = httpServletRequest.getHeader("Authorization");
////        token 분해 후 Authentication 객체 생성
////        token이 있는데 잘못된경우 , token이 없는 경우
//        try {
//            if(bearerToken != null){ //토큰이 있는경우
////            Bearer를 관례적으로 붙이는데 , 안붙였을때 에러 발생
//                if(!bearerToken.substring(0,7).equals("Bearer ")){
//                    throw new AuthenticationServiceException("Bearer 형식이 아닙니다.");
//                }
//                String token = bearerToken.substring(7);
////            token검증 및 claims 추출
//                Claims claims = Jwts.parserBuilder()
//                        .setSigningKey(secretKey)//key를 셋팅하면서 token 검증을 한거임.
//                        .build()
//                        .parseClaimsJws(token)
//                        .getBody();
////              Authentication객체 생성
//                List<GrantedAuthority> authorities = new ArrayList<>();
//                authorities.add(new SimpleGrantedAuthority("ROLE_"+claims.get("role")));
//                Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(),"",authorities);
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }
//
////        다시 filterchain으로 되돌아 가는 로직
//            filterChain.doFilter(servletRequest,servletResponse);
//        }catch (Exception e){
//            e.printStackTrace();
//            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
//            httpServletResponse.getWriter().write("token is invalid");
//
//        }
//    }
//}
