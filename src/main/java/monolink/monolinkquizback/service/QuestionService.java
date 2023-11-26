package monolink.monolinkquizback.service;

import jakarta.transaction.Transactional;
import monolink.monolinkquizback.dto.AnswerDto;
import monolink.monolinkquizback.dto.QuestionDto;
import monolink.monolinkquizback.entity.AnswerEntity;
import monolink.monolinkquizback.entity.AnswerPK;
import monolink.monolinkquizback.entity.Image;
import monolink.monolinkquizback.repository.AnswerRepository;
import monolink.monolinkquizback.repository.ImageRepository;
import monolink.monolinkquizback.utils.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Transactional
public class QuestionService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ImageRepository imageRepository;

    public List<QuestionDto> getQuestions() {
        List<AnswerEntity> answerEntities = answerRepository.findAll();
        return toDto(answerEntities);
    }

    public List<QuestionDto> saveQuestions(List<QuestionDto> questions) {
        List<AnswerEntity> answerEntities = fromDto(questions);
        List<AnswerEntity> savedAnswerEntities = answerRepository.saveAllAndFlush(answerEntities);
        return toDto(savedAnswerEntities);
    }

    private List<QuestionDto> toDto(List<AnswerEntity> answerEntities) {
        List<QuestionDto> questionDtos = new ArrayList<>();
        Map<Integer, List<AnswerEntity>> answerEntitiesByQuestionId = answerEntities.stream().collect(
                Collectors.groupingBy(a -> a.getId().getIdQuestion()));
        for (Map.Entry<Integer, List<AnswerEntity>> entry : answerEntitiesByQuestionId.entrySet()) {
            QuestionDto questionDto = QuestionDto.builder()
                    .id(entry.getKey())
                    .answers(entry.getValue().stream().map(a -> AnswerDto.builder()
                            .id(a.getId().getIdAnswer())
                            .text(a.getText())
                            .isGoodAnswer(a.getIsGoodAnswer())
                            .build()).toList())
                    .build();
            questionDtos.add(questionDto);
        }
        return questionDtos;
    }

    private List<AnswerEntity> fromDto(List<QuestionDto> questions) {
        List<AnswerEntity> answerEntities = new ArrayList<>();
        for (QuestionDto question : questions) {
            for (AnswerDto answer : question.getAnswers()) {
                AnswerPK answerPK = AnswerPK.builder()
                        .idAnswer(answer.getId())
                        .idQuestion(question.getId())
                        .build();
                AnswerEntity answerEntity = AnswerEntity.builder()
                        .id(answerPK)
                        .text(answer.getText())
                        .isGoodAnswer(answer.getIsGoodAnswer())
                        .build();
                answerEntities.add(answerEntity);
            }
        }
        return answerEntities;
    }

    public boolean isGoodResponse(Integer questionId, String answerId) {
        AnswerPK answerPK = AnswerPK.builder()
                .idAnswer(answerId)
                .idQuestion(questionId)
                .build();
        Optional<AnswerEntity> answer = answerRepository.findById(answerPK);
        if (answer.isEmpty()) {
            return false;
        }
        return answer.get().getIsGoodAnswer();
    }

    public int countQuestions() {
        List<AnswerEntity> answerEntities = answerRepository.findAll();
        return answerEntities.stream().collect(Collectors.groupingBy(a -> a.getId().getIdQuestion())).size();
    }

    public Map<Integer, String> getGoodAnswers() {
        List<AnswerEntity> answerEntities = answerRepository.findByIsGoodAnswer(true);
        return answerEntities.stream().collect(Collectors.toMap(a -> a.getId().getIdQuestion(),
                a -> a.getId().getIdAnswer()));
    }

    public QuestionDto getQuestion(Integer questionId) {
        List<AnswerEntity> answerEntities = answerRepository.findAll();
        List<AnswerEntity> answersForOnQuestion =
                answerEntities.stream().filter(a -> a.getId().getIdQuestion().equals(questionId)).toList();
        List<QuestionDto> questions = toDto(answersForOnQuestion);
        if (questions.size() == 1) {
            return questions.get(0);
        }
        return null;
    }

    public void saveImages(MultipartFile multipartFile)
            throws IOException {
        File path = new File("/images/");
        if (!path.exists()) {
            path.mkdir();
        }
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream((multipartFile.getInputStream()));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = new File(path, zipEntry.getName());
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();

        for (File file : path.listFiles()) {
            String s = Paths.get(file.getAbsolutePath()).getFileName().toString().split("\\.")[0];
            int id = Integer.valueOf(s);
            FileInputStream fileInputStream = new FileInputStream(file);
            Image image = Image.builder()
                    .id(id)
                    .name(file.getName())
                    .type("image/jpeg")
                    .image(ImageUtil.compressImage(fileInputStream.readAllBytes()))
                    .build();
            fileInputStream.close();
            imageRepository.save(image);
            Files.delete(file.toPath());
        }
    }

    public Image getImage(Integer id) {
        Optional<Image> image = imageRepository.findById(id);
        return image.map(value -> Image.builder()
                .name(value.getName())
                .type(value.getType())
                .image(ImageUtil.decompressImage(value.getImage()))
                .build()).orElse(null);
    }
}
