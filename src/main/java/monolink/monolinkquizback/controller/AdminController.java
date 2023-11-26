package monolink.monolinkquizback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import monolink.monolinkquizback.dto.*;
import monolink.monolinkquizback.entity.User;
import monolink.monolinkquizback.service.ParticipationService;
import monolink.monolinkquizback.service.QuestionService;
import monolink.monolinkquizback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin", description = "Admin management APIs")
@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(path = "/api/admin")
public class AdminController {

    @Autowired
    ParticipationService participationService;

    @Autowired
    UserService userService;

    @Autowired
    private QuestionService questionService;

    @Operation(summary = "all Results by users", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    @GetMapping(path = "/resultsByUser")
    public ResponseEntity<List<ResultByUser>> results() {
        if (participationService.countParticipations() == 0) {
            return ResponseEntity.ok().body(new ArrayList<>());
        }
        Map<User, ResultDto> resultsByUser = participationService.getResultsByUser();
        List<ResultByUser> results = new ArrayList<>();
        for (Map.Entry<User, ResultDto> entry : resultsByUser.entrySet()) {
            results.add(ResultByUser.builder()
                    .userName(entry.getKey().getUsername())
                    .percentage(entry.getValue().getPercentage() * 100)
                    .time(entry.getValue().getTime())
                    .build());
        }
        return ResponseEntity.ok().body(results);
    }

    @Operation(summary = "all Results by photo", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    @GetMapping(path = "/resultsByQuestion")
    public ResponseEntity<List<ResultByQuestion>> resultsByPhoto() {
        if (questionService.countQuestions() == 0) {
            return ResponseEntity.ok().body(new ArrayList<>());
        }
        Map<Integer, String> goodAnswers = questionService.getGoodAnswers();
        Map<User, List<ResponseDto>> responsesByUser = participationService.getResponsesByUser();
        Map<Integer, List<String>> responsesByQuestion = participationService.getResponsesByQuestion();
        Map<Integer, Double> timeByPhoto = participationService.getTimeByQuestion();
        Map<Integer, String> mostPopularResponsesByQuestion = participationService.getMostPopularResponsesByQuestion();
        List<ResultByQuestion> results = new ArrayList<>();
        for (Integer questionId : responsesByQuestion.keySet()) {
            QuestionDto questionDto = questionService.getQuestion(questionId);
            String mostPopularResponse = questionDto.getAnswers().stream()
                    .filter(a -> a.getId().equals(mostPopularResponsesByQuestion.get(questionId)))
                    .map(AnswerDto::getText).findFirst().orElse("");
            List<String> responses = responsesByQuestion.get(questionId);
            long nbGoodResponses = responses.stream().filter(r -> r.equals(goodAnswers.get(questionId))).count();
            ResultByQuestion resultByQuestion = ResultByQuestion.builder()
                    .questionId(questionId)
                    .percentage((double) nbGoodResponses / responsesByUser.size() * 100)
                    .time(timeByPhoto.get(questionId))
                    .popularResponse(mostPopularResponse)
                    .build();
            results.add(resultByQuestion);
        }
        return ResponseEntity.ok().body(results);
    }

    @Operation(summary = "count participations", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    @GetMapping(path = "/users/count")
    public ResponseEntity<Integer> countParticipations() {
        return ResponseEntity.ok().body(participationService.countParticipations());
    }

    @Operation(summary = "count finished Quiz", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    @GetMapping(path = "/quiz/count")
    public ResponseEntity<Long> countFinishedQuiz() {
        Map<User, Integer> countResponsesByUser = participationService.countResponsesByUser();
        int countQuestions = questionService.countQuestions();
        long count = countResponsesByUser.entrySet().stream().filter(e -> e.getValue() == countQuestions).count();

        return ResponseEntity.ok().body(count);
    }


}
