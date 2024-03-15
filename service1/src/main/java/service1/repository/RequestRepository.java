package service1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service1.entity.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {}