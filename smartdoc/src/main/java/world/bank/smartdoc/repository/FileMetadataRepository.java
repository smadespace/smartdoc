package world.bank.smartdoc.repository;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import world.bank.smartdoc.entity.FileMetadataEntity;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadataEntity, String> {

   // @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FileMetadataEntity> findByChecksum(String checksum);

    Optional<FileMetadataEntity> findByPath(String path);
}
