package silverpotion.postserver.post.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import silverpotion.postserver.common.dto.CommonDto;
import silverpotion.postserver.post.domain.PostCategory;
import silverpotion.postserver.post.dtos.PostCategoryDto;
import silverpotion.postserver.post.dtos.PostInitDto;
import silverpotion.postserver.post.dtos.FreePostUpdateDto;
import silverpotion.postserver.post.dtos.PostUpdateDto;
import silverpotion.postserver.post.service.PostService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("silverpotion/post")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

//    1. 게시물 생성 시, 게시물 유형 페이지 조회
    @GetMapping("/init")
    public ResponseEntity<?> initGet(){
        List<PostCategoryDto> categories = new ArrayList<>();
        for (PostCategory c : PostCategory.values()){
            categories.add(new PostCategoryDto(c.name(), c.getLabel()));
        }
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "게시물 카테고리 유형 조회 완료", categories),HttpStatus.OK);
    }

//    2. 게시물 작성시, 게시물 유형 먼저 저장(임시 저장)
    @PostMapping("/init")
    public ResponseEntity<?> initPost(@RequestBody PostInitDto dto){
        Long postId = postService.createDraftPost(dto); // postId를 받는 이유는 제목,글 저장할 때 post를 바로 찾아가서 저장할 수 있도록!
        Map<String,Object> response = new HashMap<>();
        response.put("postId",postId);
        response.put("category",dto.getPostCategory()); //게시물 유형마다 페이지가 다르니, 카테고리 데이터도 같이 넘김
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"임시 저장완료",response),HttpStatus.OK);
    }

//    3. 게시물 작성시, 제목/이미지/내용 저장(최종 저장)
    @PutMapping("/update/{postId}") // 임시저장 때, postId가 나와서 쉽게 조회 후 저장
    public ResponseEntity<?> finalSave(@PathVariable Long postId,@RequestHeader("X-User-Id") String loginId
            ,@ModelAttribute FreePostUpdateDto postUpdateDto){
        Object dto = postService.updateFinalPost(postId,loginId,postUpdateDto); //object로 받는 이유: 인터페이스를 사용해 서비스에서 분기처리를 하는데
        //인터페이스로 반환하면 json처리할 때 문제 생길 수 있어서 구현체로 받기 위해 object로 받는 것입니다! //postUpdateDto는 여기서는 FreePostUpdateDto인데
        //서비스로 넘어가면서 인터페이스로 업캐스팅을 해서 문제없습니다!
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "최종 저장완료",dto),HttpStatus.OK);
    }

//    4. 게시물 삭제
    @PostMapping("/delete/{postId}")
    public ResponseEntity<?> delete(@PathVariable Long postId,@RequestHeader("X-User-Id") String loginId){
        postService.delete(postId,loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "게시물 삭제 완료",postId),HttpStatus.OK);
    }


}