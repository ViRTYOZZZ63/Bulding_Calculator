package backend.back.service;

import backend.back.entity.Material;
import backend.back.exception.ResourceNotFoundException;
import backend.back.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    public List<Material> getAll() {
        return materialRepository.findAll();
    }

    @Transactional
    public Material updatePrice(Long id, BigDecimal newPrice) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Материал", id));
        material.setCurrentPrice(newPrice);
        material.setUpdatedAt(LocalDateTime.now());
        return materialRepository.save(material);
    }
}