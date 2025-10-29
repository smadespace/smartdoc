package world.bank.smartdoc.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import world.bank.smartdoc.model.FileSummary;
import world.bank.smartdoc.entity.FileSummaryEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileSummaryMapper {
    FileSummaryEntity toEntity(FileSummary dto);
    FileSummary toDto(FileSummaryEntity entity);
}