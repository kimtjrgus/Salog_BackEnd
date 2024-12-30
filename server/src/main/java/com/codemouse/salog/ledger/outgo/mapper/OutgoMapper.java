package com.codemouse.salog.ledger.outgo.mapper;

import com.codemouse.salog.ledger.outgo.dto.OutgoDto;
import com.codemouse.salog.ledger.outgo.entity.Outgo;
import com.codemouse.salog.tags.ledgerTags.mapper.LedgerTagMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = LedgerTagMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OutgoMapper {
    Outgo outgoPostDtoToOutgo(OutgoDto.Post requestBody);
    Outgo outgoPatchDtoToOutgo(OutgoDto.Patch requestBody);

    @Mapping(source = "ledgerTag", target = "outgoTag", qualifiedByName = "ledgerTagToLedgerTagResponseDto")
    OutgoDto.Response outgoToOutgoResponseDto(Outgo outgo);
}
