package world.bank.smartdoc.service;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import world.bank.smartdoc.entity.FileMetadataEntity;
import world.bank.smartdoc.entity.FileMetadataEntity.Format;
import world.bank.smartdoc.mapper.FileMapper;
import world.bank.smartdoc.model.FileMetadata;
import world.bank.smartdoc.repository.FileMetadataRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
public class FileMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(FileMetadataService.class);

    @Autowired
    private FileMetadataRepository fileMetadataRepository;
    @Autowired
    private FileMapper fileMapper;

    public List<FileMetadata> listFiles() {
        return fileMetadataRepository.findAll()
                .stream()
                .map(e -> fileMapper.toDto(e))
                .toList();
    }


    public String getTheContentFromFile(String id) {
        FileMetadataEntity metadata = fileMetadataRepository.getReferenceById(id);
        File file = new File(metadata.getPath());

        if (file.exists()) {
            Format type = metadata.getFormat();
            try {
                switch (type) {
                    case PDF -> {
                        return readPdfFile(file);
                    }
                    case TXT -> {
                        return readTxtFile(file);
                    }
                    case DOCX -> {
                        return readDocxFile(file);
                    }
                }
            } catch (Exception e1) {
                logger.warn(e1.toString());
            }
        }
        return ("No such file in directory!"); // <--- look so nice!
    }


    private String readTxtFile(File file) throws IOException {
        StringBuilder text = new StringBuilder();
        Files.readAllLines(file.toPath()).forEach(text::append);
        return text.toString();
    }

    private String readPdfFile(File file) throws IOException {
        String lines;
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            lines = stripper.getText(document);
        }
        return lines;
    }

    private String readDocxFile(File file) throws IOException {
        StringBuilder text = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append(System.lineSeparator());
            }
        }
        return text.toString();
    }


}
