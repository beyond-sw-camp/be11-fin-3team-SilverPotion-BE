package silverpotion.userserver.common.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import silverpotion.userserver.common.dto.CommonErrorDto;

import java.nio.file.AccessDeniedException;


@ControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityNotFound(EntityNotFoundException e){
        e.printStackTrace();
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.NOT_FOUND.value(),e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalArgument(IllegalArgumentException e){
        e.printStackTrace();
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    //MethodArgumentNotValidException은 @Valisk,@NotBlank,@Size 같은 유효성검사에서 터지는 에러로 여기서는 e.getMessage보다는
// e.getBindingResult().getFieldError().getDefaultMessage() 로 해야 정확한 에러값이 뜬다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgument(MethodArgumentNotValidException e){
        e.printStackTrace();
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getBindingResult().getFieldError().getDefaultMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity accesDeny(AccessDeniedException e) {
        e.printStackTrace();
        return new ResponseEntity(new CommonErrorDto(HttpStatus.FORBIDDEN.value(), e.getMessage()), HttpStatus.FORBIDDEN);
    }


}
