package world.bank.smartdoc.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import world.bank.smartdoc.model.FileMetadata;
import world.bank.smartdoc.entity.FileMetadataEntity;


    @Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
    public interface FileMapper {

        @Mapping(target = "format", expression = "java(dto.getFormat() != null ? world.bank.smartdoc.entity.FileMetadataEntity.Format.valueOf(dto.getFormat().name()) : null)")
        FileMetadataEntity toEntity(FileMetadata dto);

        @Mapping(target = "format", expression = "java(entity.getFormat() != null ? world.bank.smartdoc.model.FileMetadata.FormatEnum.valueOf(entity.getFormat().name()) : null)")
        FileMetadata toDto(FileMetadataEntity entity);

    }


