package monolink.monolinkquizback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import monolink.monolinkquizback.dto.ApiMessage;
import monolink.monolinkquizback.dto.ImportMessage;
import monolink.monolinkquizback.dto.QuestionDto;
import monolink.monolinkquizback.entity.Image;
import monolink.monolinkquizback.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Question", description = "Questions management APIs")
@Controller
@RequestMapping(path = "/api")
public class QuestionController {

    @Autowired
    private QuestionService service;

    @Operation(summary = "getQuestions", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    @GetMapping(path = "/questions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<QuestionDto>> getQuestions() {
        List<QuestionDto> questionDtos = service.getQuestions();
        return ResponseEntity.ok().body(questionDtos);
    }

    @Operation(summary = "postQuestions", responses = {
            @ApiResponse(responseCode = "200", description = "Ok", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = QuestionDto.class)))
    })
    @PostMapping(path = "/questions", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessage> postQuestions(@RequestBody List<QuestionDto> questions) {
        List<QuestionDto> questionDtos = service.saveQuestions(questions);
        ImportMessage importMessage = new ImportMessage("import questions ok", questionDtos.size());
        return ResponseEntity.ok().body(importMessage);
    }

    @Operation(summary = "add Images zip", responses = {
            @ApiResponse(responseCode = "200", description = "Ok")
    })
    @PostMapping(path = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessage> postImages(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        int count = service.saveImages(multipartFile);
        ImportMessage importMessage = new ImportMessage("upload zip images ok", count);
        return ResponseEntity.ok().body(importMessage);
    }

    @Operation(summary = "get Images", responses = {
            @ApiResponse(responseCode = "200", description = "Ok")
    })
    @GetMapping("/images/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getImageByName(@PathVariable("id") Integer id) {
        Image image = service.getImage(id);
        if (image == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiMessage.builder().message("image not found").build());
        }
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + image.getName() + "\"")
                .contentType(MediaType.parseMediaType("image/jpeg"))
                .body(image.getImage());
    }

}
