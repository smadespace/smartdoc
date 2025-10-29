package world.bank.smartdoc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import world.bank.smartdoc.entity.FileMetadataEntity;
import world.bank.smartdoc.model.ProcessingStatus;
import world.bank.smartdoc.repository.FileMetadataRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class FileProcessService {
    private static final Logger logger = LoggerFactory.getLogger(FileProcessService.class);


//    @Value("${files.dir.path}")
//    private String basedir;


    @Autowired
    private FileMetadataRepository repository;
    @Autowired
    private FileWatcherService fileWatcherService;

//    // @Scheduled(fixedRate = 1000)
//    public void refreshFilesInDatabase() {
//        ArrayList<String> filesPaths = (ArrayList<String>) getAllFiles(basedir);
//        filesPaths.forEach(this::saveFileMetadata);
//    }


    public ProcessingStatus refreshFilesInDatabaseFromReq(String basedir) {
        logger.info("start from request...");
        ArrayList<String> filesPaths = (ArrayList<String>) getAllFiles(basedir);
        ProcessingStatus processingStatus = new ProcessingStatus();
        filesPaths.forEach(this::saveFileMetadata);
        int totalNumOfFiles = filesPaths.size();
        processingStatus.setStatus("done");
        processingStatus.setTotalFiles(totalNumOfFiles);
        return processingStatus;
    }


    public List<String> getAllFiles(String baseFilesDir) {
        try {
            return Files.walk(Path.of(baseFilesDir))
                    .filter(Files::isRegularFile).filter(f -> determineFormat(f.toFile().getName()) != null)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error while walking directory tree: {}", baseFilesDir, e);
            return new ArrayList<>();
        }
    }


    public void saveFileMetadata(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            logger.info("File not found: {}", filePath);
            return;
        } else if (determineFormat(file.getName()) == null) {
            return;
        }

        String checksum = generateChecksum(file);
        repository.findByChecksum(checksum)
                .orElseGet(() -> {
                    FileMetadataEntity entity = new FileMetadataEntity();
                    entity.setName(file.getName());
                    entity.setPath(file.getAbsolutePath());
                    entity.setSize(file.length());
                    entity.setChecksum(checksum);
                    entity.setCreatedAt(OffsetDateTime.now());
                    entity.setPresent(true);
                    entity.setFormat(determineFormat(file.getName()));
                    entity.setUpdatedAt(null);
                    return repository.save(entity);
                });
    }


    private FileMetadataEntity.Format determineFormat(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return FileMetadataEntity.Format.PDF;
        if (lower.endsWith(".docx")) return FileMetadataEntity.Format.DOCX;
        if (lower.endsWith(".txt")) return FileMetadataEntity.Format.TXT;
        logger.warn("Unsupported file format: {}", fileName);
        return null;
    }

    private String generateChecksum(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate checksum for file: " + file.getName(), e);
        }
    }
}