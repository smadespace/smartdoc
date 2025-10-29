package world.bank.smartdoc.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Entity
@Table(name = "file_summary")
public class FileSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "summary_text", columnDefinition = "TEXT")
    private String summaryText;

    // Relation to FileMetadata
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileMetadataEntity file;


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getSummaryText() {
        return summaryText;
    }
    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public FileMetadataEntity getFile() {
        return file;
    }
    public void setFile(FileMetadataEntity file) {
        this.file = file;
    }
}
