package org.example.productservice.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface DateMapper {

    @Named("localDateToInstantStart")
    default Instant localDateToInstantStart(LocalDate date) {
        return date == null ? null : date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    @Named("localDateToInstantEnd")
    default Instant localDateToInstantEnd(LocalDate date) {
        return date == null ? null : date.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toInstant();
    }
}