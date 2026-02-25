package ru.document.service.reposiory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.document.service.entity.History;

@Repository
public interface DocumentHistoryRepository extends JpaRepository<History, Long> {
}