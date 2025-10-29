package world.bank.smartdoc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import world.bank.smartdoc.entity.FileMetadataEntity;
import world.bank.smartdoc.entity.FileSummaryEntity;
import world.bank.smartdoc.model.FileSummary;
import world.bank.smartdoc.repository.FileMetadataRepository;
import world.bank.smartdoc.repository.FileSummaryRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class OpenAiSummarizerService {

    @Value("${openapi.key}")
    private String API_KEY;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini"; // or "gpt-4-turbo"


    @Autowired
    private FileSummaryRepository fileSummaryRepository;


    @Autowired
    private FileMetadataRepository fileMetadataRepository;


    @Transactional
    public void updateFileSummary(String fileId) {

        FileMetadataEntity metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));

        String content;
        String summary = "";
        try {
            content = Files.readString(Path.of(metadata.getPath()));
            summary = summarizeTextFile(content);
        } catch (Exception e) {
            summary="mo summary";
        }


        FileSummaryEntity summaryEntity = fileSummaryRepository.findByFile_Id(metadata.getId())
                .orElseGet(() -> {
                    FileSummaryEntity newEntity = new FileSummaryEntity();
                    newEntity.setFile(metadata);
                    return newEntity;
                });

        summaryEntity.setSummaryText(summary);


        fileSummaryRepository.save(summaryEntity);
    }


    public FileSummary getSummaryByFileId(String fileId) {

        FileSummary fileSummary = new FileSummary();

        String sum = fileSummaryRepository.findByFile_Id(fileId)
                .map(FileSummaryEntity::getSummaryText)
                .orElse("No summary found for file: " + fileId);

        fileSummary.setSummaryText(sum);
        fileSummary.setId(fileId);

        return fileSummary;
    }


    public String summarizeTextFile(String filePath) throws Exception {

        String content = Files.readString(Path.of(filePath));


        Map<String, Object> message = Map.of(
                "role", "user",
                "content", "Summarize the following text:\n\n" + content
        );

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(message),
                "temperature", 0.3
        );


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);


        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );


        var choices = (List<Map<String, Object>>) response.getBody().get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
            return messageObj.get("content").toString();
        }

        return "No summary generated.";
    }
}
