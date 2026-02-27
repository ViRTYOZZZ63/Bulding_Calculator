package backend.back.service;

import backend.back.dto.request.FoundationParamsRequest;
import backend.back.dto.request.FrameParamsRequest;
import backend.back.entity.CalculationResultItem;
import backend.back.util.FoundationCalculator;
import backend.back.util.FrameCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculationEngineService {

    private final FrameCalculator frameCalculator;
    private final FoundationCalculator foundationCalculator;

    public List<CalculationResultItem> calculateFrame(FrameParamsRequest params) {
        return frameCalculator.calculate(params);
    }

    public List<CalculationResultItem> calculateFoundation(FoundationParamsRequest params) {
        return foundationCalculator.calculate(params);
    }
}