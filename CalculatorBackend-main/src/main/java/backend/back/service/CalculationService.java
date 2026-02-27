package backend.back.service;

import backend.back.dto.request.CalculationRequest;
import backend.back.dto.response.CalculationResponse;
import backend.back.entity.Calculation;
import backend.back.entity.CalculationElement;
import backend.back.entity.CalculationResultItem;
import backend.back.entity.User;
import backend.back.entity.enums.CalculationStatus;
import backend.back.exception.ResourceNotFoundException;
import backend.back.exception.UnauthorizedActionException;
import backend.back.mapper.CalculationMapper;
import backend.back.repository.CalculationRepository;
import backend.back.repository.ClientRepository;
import backend.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationService {

    private final CalculationRepository calculationRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final CalculationMapper calculationMapper;

    public List<CalculationResponse> getByClientId(Long clientId) {
        return calculationRepository.findAllByClientId(clientId).stream()
                .map(calculationMapper::toResponse)
                .toList();
    }

    public CalculationResponse getById(Long id) {
        return calculationMapper.toResponse(findById(id));
    }

    @Transactional
    public CalculationResponse create(Long clientId, CalculationRequest request) {
        Calculation calculation = new Calculation();
        calculation.setClient(clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Клиент", clientId)));
        calculation.setCreatedBy(currentUser());
        calculation.setConstructionAddress(request.getConstructionAddress());
        calculation.setStatus(CalculationStatus.ACTUAL);
        calculation.setCreatedAt(LocalDateTime.now());
        calculation.setUpdatedAt(LocalDateTime.now());
        calculation.setPricesFixedUntil(LocalDateTime.now().plusDays(10));
        return calculationMapper.toResponse(calculationRepository.save(calculation));
    }

    @Transactional
    public CalculationResponse updateStatus(Long id, CalculationStatus newStatus) {
        Calculation calculation = findById(id);
        if (calculation.getStatus() == CalculationStatus.CONTRACT_SIGNED) {
            throw new UnauthorizedActionException("Нельзя изменить статус расчёта с заключённым договором");
        }
        calculation.setStatus(newStatus);
        calculation.setUpdatedAt(LocalDateTime.now());
        // При актуализации — фиксируем цены ещё на 10 дней
        if (newStatus == CalculationStatus.ACTUAL) {
            calculation.setPricesFixedUntil(LocalDateTime.now().plusDays(10));
        }
        return calculationMapper.toResponse(calculationRepository.save(calculation));
    }

    @Transactional
    public CalculationResponse copy(Long id) {
        Calculation original = findById(id);
        Calculation copy = new Calculation();
        copy.setClient(original.getClient());
        copy.setCreatedBy(currentUser());
        copy.setConstructionAddress(original.getConstructionAddress());
        copy.setStatus(CalculationStatus.ACTUAL);
        copy.setCreatedAt(LocalDateTime.now());
        copy.setUpdatedAt(LocalDateTime.now());
        copy.setPricesFixedUntil(LocalDateTime.now().plusDays(10));
        // Копируем элементы (inputParams), результат будет пересчитан отдельно
        List<CalculationElement> copiedElements = new ArrayList<>();
        for (CalculationElement el : original.getElements()) {
            CalculationElement newEl = new CalculationElement();
            newEl.setCalculation(copy);
            newEl.setElementType(el.getElementType());
            newEl.setInputParams(el.getInputParams());
            newEl.setCreatedAt(LocalDateTime.now());
            newEl.setUpdatedAt(LocalDateTime.now());
            copiedElements.add(newEl);
        }
        copy.setElements(copiedElements);
        return calculationMapper.toResponse(calculationRepository.save(copy));
    }

    @Transactional
    public void delete(Long id) {
        Calculation calculation = findById(id);
        if (calculation.getStatus() == CalculationStatus.CONTRACT_SIGNED) {
            throw new UnauthorizedActionException("Нельзя удалить расчёт с заключённым договором");
        }
        calculationRepository.delete(calculation);
    }

    /**
     * Планировщик: каждую ночь в 00:00 переводит истёкшие расчёты в NOT_ACTUAL.
     * Требование из ТЗ: цены фиксируются на 10 дней.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOutdatedCalculations() {
        List<Calculation> expired = calculationRepository
                .findAllByStatusAndPricesFixedUntilBefore(
                        CalculationStatus.ACTUAL, LocalDateTime.now());
        expired.forEach(c -> c.setStatus(CalculationStatus.NOT_ACTUAL));
        calculationRepository.saveAll(expired);
        log.info("Переведено в NOT_ACTUAL расчётов: {}", expired.size());
    }

    private Calculation findById(Long id) {
        return calculationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Расчёт", id));
    }

    private User currentUser() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLogin(login).orElseThrow();
    }
}