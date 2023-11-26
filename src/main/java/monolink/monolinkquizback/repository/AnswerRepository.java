package monolink.monolinkquizback.repository;

import monolink.monolinkquizback.entity.AnswerEntity;
import monolink.monolinkquizback.entity.AnswerPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, AnswerPK> {
    @Query(value = "select count(*) from AnswerEntity")
    int countQuestions();

    List<AnswerEntity> findByIsGoodAnswer(boolean b);
}
