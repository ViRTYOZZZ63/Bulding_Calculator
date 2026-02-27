package backend.back.repository;

// Все стандартные CRUD методы наследуются от JpaRepository

import backend.back.entity.*;
import backend.back.entity.enums.CalculationStatus;
import backend.back.entity.enums.ElementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    boolean existsByLogin(String login);
}









