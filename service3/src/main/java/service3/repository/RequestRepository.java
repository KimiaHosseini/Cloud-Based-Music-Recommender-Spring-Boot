package service3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service3.entity.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByStatus(String status);
}

