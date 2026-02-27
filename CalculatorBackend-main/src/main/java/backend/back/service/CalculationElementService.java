package backend.back.service;

import backend.back.dto.request.FoundationParamsRequest;
import backend.back.dto.request.FrameParamsRequest;
import backend.back.dto.response.CalculationElementResponse;
import backend.back.entity.Calculation;
import backend.back.entity.CalculationElement;
import backend.back.entity.CalculationResultItem;
import backend.back.entity.enums.CalculationStatus;
import backend.back.entity.enums.ElementType;
import backend.back.exception.ResourceNotFoundException;
import backend.back.exception.UnauthorizedActionException;
import backend.back.mapper.CalculationElementMapper;
import backend.back.repository.CalculationElementRepository;
import backend.back.repository.CalculationRepository;
import backend.back.repository.CalculationResultItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalculationElementService {

    private final CalculationRepository calculationRepository;
    private final CalculationElementRepository elementRepository;
    private final CalculationResultItemRepository resultItemRepository;
    private final CalculationEngineService engineService;
    private final CalculationElementMapper elementMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public CalculationElementResponse addOrUpdateFrame(Long calculationId, FrameParamsRequest request) {
        Calculation calculation = getCalculation(calculationId);
        checkEditable(calculation);

        CalculationElement element = elementRepository
                .findByCalculationIdAndElementType(calculationId, ElementType.FRAME)
                .orElseGet(() -> {
                    CalculationElement e = new CalculationElement();
                    e.setCalculation(calculation);
                    e.setElementType(ElementType.FRAME);
                    e.setCreatedAt(LocalDateTime.now());
                    return e;
                });

        // Сохраняем входные параметры как JSONB для истории
        element.setInputParams(objectMapper.convertValue(request, Map.class));
        element.setUpdatedAt(LocalDateTime.now());
        CalculationElement saved = elementRepository.save(element);

        // Удаляем старые результаты и пересчитываем
        resultItemRepository.deleteAllByCalculationElementId(saved.getId());
        List<CalculationResultItem> items = engineService.calculateFrame(request);
        items.forEach(item -> item.setCalculationElement(saved));
        resultItemRepository.saveAll(items);
        saved.setResultItems(items);

        return elementMapper.toResponse(saved);
    }

    @Transactional
    public CalculationElementResponse addOrUpdateFoundation(Long calculationId, FoundationParamsRequest request) {
        Calculation calculation = getCalculation(calculationId);
        checkEditable(calculation);

        CalculationElement element = elementRepository
                .findByCalculationIdAndElementType(calculationId, ElementType.FOUNDATION)
                .orElseGet(() -> {
                    CalculationElement e = new CalculationElement();
                    e.setCalculation(calculation);
                    e.setElementType(ElementType.FOUNDATION);
                    e.setCreatedAt(LocalDateTime.now());
                    return e;
                });

        element.setInputParams(objectMapper.convertValue(request, Map.class));
        element.setUpdatedAt(LocalDateTime.now());
        CalculationElement saved = elementRepository.save(element);

        resultItemRepository.deleteAllByCalculationElementId(saved.getId());
        List<CalculationResultItem> items = engineService.calculateFoundation(request);
        items.forEach(item -> item.setCalculationElement(saved));
        resultItemRepository.saveAll(items);
        saved.setResultItems(items);

        return elementMapper.toResponse(saved);
    }

    private Calculation getCalculation(Long id) {
        return calculationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Расчёт", id));
    }

    private void checkEditable(Calculation calculation) {
        if (calculation.getStatus() == CalculationStatus.CONTRACT_SIGNED) {
            throw new UnauthorizedActionException(
                    "Нельзя изменить элементы расчёта с заключённым договором");
        }
    }
}