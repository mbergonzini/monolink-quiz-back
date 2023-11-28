package monolink.monolinkquizback.service;

import jakarta.transaction.Transactional;
import monolink.monolinkquizback.dto.QuestionDto;
import monolink.monolinkquizback.dto.ResponseDto;
import monolink.monolinkquizback.dto.ResultDto;
import monolink.monolinkquizback.entity.ParticipationEntity;
import monolink.monolinkquizback.entity.Response;
import monolink.monolinkquizback.entity.User;
import monolink.monolinkquizback.repository.ParticipationRepository;
import monolink.monolinkquizback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class ParticipationService {

    @Autowired
    private ParticipationRepository repo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private QuestionService questionService;


    private ParticipationEntity newParticipation(UUID userId) {
        Optional<User> user = userRepo.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("user not found");
        }
        ParticipationEntity participationToSave = ParticipationEntity.builder()
                .id(UUID.randomUUID())
                .responses(new ArrayList<>())
                .user(user.get())
                .build();
        return repo.saveAndFlush(participationToSave);
    }

    private List<ResponseDto> toDto(ParticipationEntity participationEntity) {
        return participationEntity.getResponses().stream()
                .map(response -> ResponseDto.builder()
                        .questionId(response.getQuestionId())
                        .answerId(response.getAnswerId())
                        .time(response.getTime())
                        .build()).toList();
    }

    public List<ResponseDto> addResponse(UUID userId, ResponseDto responseDto) {
        Optional<User> userEntity = userRepo.findById(userId);
        if (userEntity.isEmpty()) {
            throw new RuntimeException("user not found");
        }
        Optional<ParticipationEntity> userParticipation = repo.findByUser(userEntity.get());
        ParticipationEntity participationToSave = userParticipation.orElseGet(() -> newParticipation(userId));
        Response response = Response.builder()
                .questionId(responseDto.getQuestionId())
                .answerId(responseDto.getAnswerId())
                .time(responseDto.getTime())
                .build();
        participationToSave.getResponses().add(response);
        ParticipationEntity participationSaved = repo.saveAndFlush(participationToSave);

        return toDto(participationSaved);
    }

    public List<ResponseDto> getResponses(UUID id) {
        Optional<User> userEntity = userRepo.findById(id);
        if (userEntity.isEmpty()) {
            throw new RuntimeException("user not found");
        }
        Optional<ParticipationEntity> userParticipation = repo.findByUser(userEntity.get());
        if (userParticipation.isEmpty()) {
            return new ArrayList<>();
        }
        return toDto(userParticipation.get());
    }

    public ResultDto getParticipantResult(UUID id) {
        Optional<User> userEntity = userRepo.findById(id);
        if (userEntity.isEmpty()) {
            throw new RuntimeException("user not found");
        }
        Optional<ParticipationEntity> userParticipation = repo.findByUser(userEntity.get());
        if (userParticipation.isEmpty()) {
            throw new RuntimeException("participation not found");
        }

        ParticipationEntity participation = userParticipation.get();
        long countGoodResponse = participation.getResponses().stream()
                .filter(response -> questionService.isGoodResponse(response.getQuestionId(), response.getAnswerId()))
                .count();
        return ResultDto.builder()
                .percentage((double) countGoodResponse / questionService.getQuestions().size() * 100)
                .time(participation.getResponses().stream().mapToDouble(Response::getTime).sum())
                .note(countGoodResponse + " / " + questionService.getQuestions().size())
                .build();
    }

    public int getCountResponse(UUID id) {
        Optional<User> userEntity = userRepo.findById(id);
        if (userEntity.isEmpty()) {
            throw new RuntimeException("user not found");
        }
        Optional<ParticipationEntity> userParticipation = repo.findByUser(userEntity.get());
        if (userParticipation.isEmpty()) {
            throw new RuntimeException("participation not found");
        }
        return userParticipation.get().getResponses().size();
    }

    public ResponseDto getResponseForQuestion(Integer questionId, UUID id) {
        Optional<User> userEntity = userRepo.findById(id);
        if (userEntity.isEmpty()) {
            throw new RuntimeException("user not found");
        }
        Optional<ParticipationEntity> userParticipation = repo.findByUser(userEntity.get());
        if (userParticipation.isEmpty()) {
            throw new RuntimeException("participation not found");
        }
        for (Response r : userParticipation.get().getResponses()) {
            if (Objects.equals(questionId, r.getQuestionId())) {
                return ResponseDto.builder()
                        .questionId(r.getQuestionId())
                        .answerId(r.getAnswerId())
                        .time(r.getTime())
                        .build();
            }
        }
        return null;
    }

    public int countParticipations() {
        return repo.findAll().size();
    }

    public Map<User, Integer> countResponsesByUser() {
        Map<User, Integer> countResponsesByUser = new HashMap<>();
        List<ParticipationEntity> participations = repo.findAll();
        for (ParticipationEntity participation : participations) {
            countResponsesByUser.put(participation.getUser(), participation.getResponses().size());
        }
        return countResponsesByUser;
    }

    public Map<User, ResultDto> getResultsByUser() {
        Map<User, ResultDto> resultsByUser = new HashMap<>();
        List<ParticipationEntity> participations = repo.findAll();
        for (ParticipationEntity participation : participations) {
            long countGoodResponse = participation.getResponses().stream()
                    .filter(response -> questionService.isGoodResponse(response.getQuestionId(), response.getAnswerId()))
                    .count();
            ResultDto result = ResultDto.builder()
                    .percentage((double) countGoodResponse / questionService.getQuestions().size())
                    .time(participation.getResponses().stream().mapToDouble(Response::getTime).sum())
                    .build();
            resultsByUser.put(participation.getUser(), result);
        }
        return resultsByUser;
    }

    public Map<User, List<ResponseDto>> getResponsesByUser() {
        Map<User, List<ResponseDto>> responsesByUser = new HashMap<>();
        List<ParticipationEntity> participations = repo.findAll();
        for (ParticipationEntity participation : participations) {
            List<ResponseDto> responses = participation.getResponses().stream()
                    .map(response -> ResponseDto.builder()
                            .questionId(response.getQuestionId())
                            .answerId(response.getAnswerId())
                            .time(response.getTime())
                            .build()).toList();
            responsesByUser.put(participation.getUser(), responses);
        }
        return responsesByUser;
    }

    public Map<Integer, List<String>> getResponsesByQuestion() {
        Map<Integer, List<String>> responsesByQuestion = new HashMap<>();
        List<ParticipationEntity> participations = repo.findAll();
        List<ResponseDto> allResponses = participations.stream()
                .flatMap(p -> p.getResponses().stream())
                .map(response -> ResponseDto.builder()
                        .questionId(response.getQuestionId())
                        .answerId(response.getAnswerId())
                        .time(response.getTime())
                        .build())
                .toList();
        List<QuestionDto> questions = questionService.getQuestions();
        for (QuestionDto question : questions) {
            List<String> answers = allResponses.stream()
                    .filter(response -> Objects.equals(response.getQuestionId(), question.getId()))
                    .map(ResponseDto::getAnswerId)
                    .toList();
            responsesByQuestion.put(question.getId(), answers);
        }
        return responsesByQuestion;
    }

    public Map<Integer, Double> getTimeByQuestion() {
        Map<Integer, Double> timeByQuestion = new HashMap<>();
        List<ParticipationEntity> participations = repo.findAll();
        List<ResponseDto> allResponses = participations.stream()
                .flatMap(p -> p.getResponses().stream())
                .map(response -> ResponseDto.builder()
                        .questionId(response.getQuestionId())
                        .answerId(response.getAnswerId())
                        .time(response.getTime())
                        .build())
                .toList();
        List<QuestionDto> questions = questionService.getQuestions();
        for (QuestionDto question : questions) {
            OptionalDouble time = allResponses.stream()
                    .filter(response -> Objects.equals(response.getQuestionId(), question.getId()))
                    .mapToDouble(ResponseDto::getTime)
                    .average();
            if (time.isEmpty()) {
                timeByQuestion.put(question.getId(), 0d);
                continue;
            }
            timeByQuestion.put(question.getId(), time.getAsDouble());
        }
        return timeByQuestion;
    }

    public Map<Integer, String> getMostPopularResponsesByQuestion() {
        Map<Integer, String> mostPopularResponsesByQuestion = new HashMap<>();
        Map<Integer, List<String>> responsesByQuestion = getResponsesByQuestion();
        for (Map.Entry<Integer, List<String>> entry : responsesByQuestion.entrySet()) {
            Integer questionId = entry.getKey();
            List<String> responses = entry.getValue();
            String mostPopularResponse = responses.stream()
                    .max(Comparator.comparingInt(o -> Collections.frequency(responses, o)))
                    .orElse("");
            mostPopularResponsesByQuestion.put(questionId, mostPopularResponse);
        }
        return mostPopularResponsesByQuestion;
    }
}
