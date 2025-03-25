package silverpotion.post.common.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import silverpotion.post.common.domain.CommonErrorDto;

//public class CommonExceptionHandler {
//    @ExceptionHandler(EntityNotFoundException.class)
//    public ResponseEntity<?> entityNotFound(EntityNotFoundException e){
//        e.printStackTrace();
//        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.NOT_FOUND.value(),e.getMessage()), HttpStatus.NOT_FOUND);
//    }
//}
