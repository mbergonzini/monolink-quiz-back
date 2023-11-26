package monolink.monolinkquizback.repository;

import monolink.monolinkquizback.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Integer> {
}
