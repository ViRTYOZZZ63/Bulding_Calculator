package backend.back.controller;

import backend.back.dto.request.CalculationRequest;
import backend.back.dto.request.FoundationParamsRequest;
import backend.back.dto.request.FrameParamsRequest;
import backend.back.dto.response.CalculationElementResponse;
import backend.back.dto.response.CalculationResponse;
import backend.back.entity.enums.CalculationStatus;
import backend.back.service.CalculationElementService;
import backend.back.service.CalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calculations")
@RequiredArgsConstructor
@Tag(name = "Расчёты")
public class CalculationController {

    private final CalculationService calculationService;
    private final CalculationElementService calculationElementService;

    // ===== Расчёты =====

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Список расчётов клиента")
    public ResponseEntity<List<CalculationResponse>> getByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(calculationService.getByClientId(clientId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить расчёт по ID")
    public ResponseEntity<CalculationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(calculationService.getById(id));
    }

    @PostMapping("/client/{clientId}")
    @Operation(summary = "Создать новый расчёт для клиента")
    public ResponseEntity<CalculationResponse> create(@PathVariable Long clientId,
                                                      @RequestBody CalculationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(calculationService.create(clientId, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Изменить статус расчёта",
            description = "Допустимые статусы: ACTUAL, NOT_ACTUAL, CONTRACT_SIGNED")
    public ResponseEntity<CalculationResponse> updateStatus(@PathVariable Long id,
                                                            @RequestBody Map<String, String> body) {
        CalculationStatus status = CalculationStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(calculationService.updateStatus(id, status));
    }

    @PostMapping("/{id}/copy")
    @Operation(summary = "Скопировать расчёт (пересчёт в актуальных ценах)")
    public ResponseEntity<CalculationResponse> copy(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calculationService.copy(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить расчёт")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        calculationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Конструктивные элементы (каркас / фундамент) =====

    @PostMapping("/{calculationId}/frame")
    @Operation(summary = "Добавить / пересчитать каркас",
            description = "Если каркас уже есть — пересчитывает с новыми параметрами")
    public ResponseEntity<CalculationElementResponse> addFrame(
            @PathVariable Long calculationId,
            @Valid @RequestBody FrameParamsRequest request) {
        return ResponseEntity.ok(calculationElementService.addOrUpdateFrame(calculationId, request));
    }

    @PostMapping("/{calculationId}/foundation")
    @Operation(summary = "Добавить / пересчитать фундамент")
    public ResponseEntity<CalculationElementResponse> addFoundation(
            @PathVariable Long calculationId,
            @Valid @RequestBody FoundationParamsRequest request) {
        return ResponseEntity.ok(calculationElementService.addOrUpdateFoundation(calculationId, request));
    }
}