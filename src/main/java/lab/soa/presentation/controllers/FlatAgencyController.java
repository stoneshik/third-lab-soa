package lab.soa.presentation.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lab.soa.domain.models.BalconyType;
import lab.soa.domain.models.PriceType;
import lab.soa.domain.models.SortType;
import lab.soa.domain.models.TransportType;
import lab.soa.presentation.dto.responses.flat.FlatResponseByIdDto;
import lab.soa.presentation.dto.responses.flat.WrapperListFlatsResponseDto;
import lab.soa.service.services.flat.FlatService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/v1/flats/agency")
@RequiredArgsConstructor
public class FlatAgencyController {
    private final FlatService flatService;


    @GetMapping(
        value = "/find-with-balcony/{priceType}/{balconyType}",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<FlatResponseByIdDto> findWithBalcony(
        @PathVariable PriceType priceType,
        @PathVariable BalconyType balconyType
    ) {
        return ResponseEntity.ok(
            flatService.findWithBalcony(
                priceType,
                balconyType
            )
        );
    }

    @GetMapping(
        value = "/get-ordered-by-time-to-metro/{transportType}/{sortType}",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<WrapperListFlatsResponseDto> getOrderedByTimeToMetro(
        @PathVariable TransportType transportType,
        @PathVariable SortType sortType,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
            flatService.getOrderedByTimeToMetro(
                transportType,
                sortType,
                page,
                size
            )
        );
    }
}
