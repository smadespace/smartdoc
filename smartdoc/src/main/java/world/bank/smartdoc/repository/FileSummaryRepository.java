package world.bank.smartdoc.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import world.bank.smartdoc.entity.FileSummaryEntity;
import java.util.Optional;

@Repository
public interface FileSummaryRepository extends JpaRepository<FileSummaryEntity, String> {
    Optional<FileSummaryEntity> findByFile_Id(String fileId);
}
