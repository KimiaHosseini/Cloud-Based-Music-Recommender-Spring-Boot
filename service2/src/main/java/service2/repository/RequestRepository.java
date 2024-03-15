package service2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import service2.entity.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {}

