package world.bank.smartdoc.entity;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetDateTime;


@ToString
@EqualsAndHashCode
@Entity
@Getter
@Table(name = "file_metadata")
public class FileMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Format format;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false, unique = true)
    private String checksum;


    @Column(nullable = false)
    private boolean present;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public Format getFormat() {
        return format;
    }

    public Long getSize() {
        return size;
    }

    public String getChecksum() {
        return checksum;
    }


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    // ---- Enums ----
    public enum Format {
        PDF, DOCX, TXT
    }

    public enum Status {
        PENDING, PROCESSED, FAILED
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }


    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
