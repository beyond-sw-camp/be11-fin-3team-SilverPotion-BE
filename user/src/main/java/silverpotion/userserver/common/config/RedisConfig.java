//package silverpotion.user.common.config;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnection;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisConfig {
//    @Value("${spring.redis.host}")
//    private String host;
//
//    @Value("${spring.redis.port}")
//    private int port;
//
//    //    redis에 접근하기 위한 접근(connection)객체
//    @Bean
//    @Qualifier("rtdb")
//    public RedisConnectionFactory redisConnectionFactory(){
//        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
//        configuration.setHostName(host);
//        configuration.setPort(port);
//        configuration.setDatabase(0);//0번 저장소에 Rt 저장 (0~15번 사용가능)
//
//        return new LettuceConnectionFactory(configuration);
//    }
////    redis에 저장할 key, value의 타입지정한 template 객체 생성
//    @Bean
//    @Qualifier("rtdb")
//    public RedisTemplate<String ,Object> redisTemplate(@Qualifier("rtdb") RedisConnectionFactory redisConnectionFactory){
//        RedisTemplate<String ,Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        return redisTemplate;
//    }
//
//    @Bean
//    @Qualifier("stockinventory")
//    public RedisConnectionFactory stockConnectionFactory(){
//        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
//        configuration.setHostName(host);
//        configuration.setPort(port);
//        configuration.setDatabase(1);
//
//        return new LettuceConnectionFactory(configuration);
//    }
//    @Bean
//    @Qualifier("stockinventory")
//    public RedisTemplate<String ,String> stockRedisTemplate(
//            @Qualifier("stockinventory") RedisConnectionFactory redisConnectionFactory){
//        RedisTemplate<String ,String> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        return redisTemplate;
//    }
//}
