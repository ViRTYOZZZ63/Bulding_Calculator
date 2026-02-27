package backend.back.controller;

import backend.back.entity.Material;
import backend.back.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Tag(name = "Справочник материалов")
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    @Operation(summary = "Список всех материалов с текущими ценами")
    public ResponseEntity<List<Material>> getAll() {
        return ResponseEntity.ok(materialService.getAll());
    }

    @PutMapping("/{id}/price")
    @Operation(summary = "Обновить цену материала")
    public ResponseEntity<Material> updatePrice(@PathVariable Long id,
                                                @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(materialService.updatePrice(id, body.get("price")));
    }
}