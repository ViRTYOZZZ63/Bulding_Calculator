package backend.back.mapper;

import backend.back.dto.response.ClientResponse;
import backend.back.entity.Calculation;
import backend.back.entity.Client;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "calculations", source = "calculations")
    ClientResponse toResponse(Client client);

    @Mapping(target = "status", expression = "java(c.getStatus().name())")
    ClientResponse.CalculationShortResponse toShortResponse(Calculation c);
}