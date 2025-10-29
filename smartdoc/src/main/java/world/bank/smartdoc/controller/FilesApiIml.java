package world.bank.smartdoc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import world.bank.smartdoc.api.FilesApi;
import world.bank.smartdoc.model.FileMetadata;
import world.bank.smartdoc.model.FileSummary;
import world.bank.smartdoc.service.FileMetadataService;
import world.bank.smartdoc.service.OpenAiSummarizerService;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FilesApiIml implements FilesApi {

    private static final Logger logger = LoggerFactory.getLogger(FilesApiIml.class);

    @Autowired
    private FileMetadataService fileMetadataService;

    @Autowired
    private OpenAiSummarizerService openAiSummarizerService;


    @Override
    public ResponseEntity<String> getFileContent(String id) {
        return ResponseEntity.status(200).body(fileMetadataService.getTheContentFromFile(id));
    }

    @Override
    public ResponseEntity<FileSummary> getFileSummary(String id) {
        openAiSummarizerService.updateFileSummary(id);
        FileSummary fileSummary = openAiSummarizerService.getSummaryByFileId(id);
        return ResponseEntity.status(200).body(fileSummary);
    }

    @Override
    public ResponseEntity<List<FileMetadata>> listFiles() {
        return ResponseEntity.status(200).body(fileMetadataService.listFiles());
    }
}
