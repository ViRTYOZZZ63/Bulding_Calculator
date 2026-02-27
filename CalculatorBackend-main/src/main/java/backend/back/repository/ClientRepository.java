package backend.back.repository;


import backend.back.entity.Client;
import backend.back.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findAllByCreatedBy(User user);           // клиенты менеджера
    Page<Client> findAll(Pageable pageable);               // с пагинацией
}
