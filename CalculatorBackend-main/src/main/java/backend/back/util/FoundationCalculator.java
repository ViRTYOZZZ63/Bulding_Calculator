package backend.back.util;


import backend.back.dto.request.FoundationParamsRequest;
import backend.back.entity.CalculationResultItem;
import backend.back.entity.Material;
import backend.back.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Калькулятор свайного фундамента (забивные бетонные сваи).
 * Алгоритм взят из файла свайный_фундамент.xlsx, лист "ЗАБИВНЫЕ СВАИ"
 *
 * Конструкция:
 * - Забивные бетонные сваи (покупные) + ростверк (монолитный)
 *
 * Константы:
 * - Шаг свай: 2 м
 * - Высота ростверка: 0.4 м
 * - Глубина ростверка: 0.3 м
 * - Арматура ростверка продольная: 4 прутка вдоль периметра, прут 6 м
 * - Арматура ростверка поперечная: шаг 30 см, куски по 20 и 30 см
 * - Опалубка: доска 30×100, толщина 0.03 м, запас по высоте 0.1 м
 * - Крепёж опалубки: брус 50×50, шаг 0.7 м, длина 0.5 м (2 стороны)
 */
@Component
@RequiredArgsConstructor
public class FoundationCalculator {

    private static final double PILE_STEP = 2.0;             // шаг свай, м
    private static final double ROSTWERK_HEIGHT = 0.4;       // высота ростверка, м
    private static final double ROSTWERK_DEPTH = 0.3;        // ширина (глубина) ростверка, м
    private static final double REBAR14_LENGTH = 6.0;        // длина прутка 14мм, м
    private static final double REBAR14_ROWS = 4;            // 4 продольных прутка в ростверке
    private static final double REBAR8_STEP = 0.3;           // шаг поперечной арматуры, м
    private static final double REBAR8_PIECE_LEN = 0.4 + 0.6; // 2 куска по 20 + 2 куска по 30 см = 1.0 м на хомут
    private static final double FORMWORK_THICKNESS = 0.03;   // толщина доски опалубки, м
    private static final double FORMWORK_HEIGHT_MARGIN = 0.1;// запас по высоте, м
    private static final double BRUS_STEP = 0.7;             // шаг крепежа бруса, м
    private static final double BRUS_LENGTH = 0.5;           // длина бруска, м

    private final MaterialRepository materialRepository;

    /**
     * Выполняет расчёт свайного фундамента.
     *
     * @param params входные параметры из запроса
     * @return список строк результата расчёта
     */
    public List<CalculationResultItem> calculate(FoundationParamsRequest params) {
        List<CalculationResultItem> items = new ArrayList<>();

        double totalLinear = params.getExternalPerimeter() + params.getInnerWallLength();

        // ===== СВАИ =====
        int pileCount = (int) Math.ceil(totalLinear / PILE_STEP);
        BigDecimal pilePrice = getPrice(params.getPileType());
        items.add(buildItem("Сваи", params.getPileType(), "шт",
                pileCount, pilePrice));

        // ===== РОСТВЕРК =====

        // Бетон для ростверка: длина × высота × ширина
        double rostverkVolume = round2(totalLinear * ROSTWERK_HEIGHT * ROSTWERK_DEPTH);
        items.add(buildItem("Ростверк", params.getConcreteGrade(), "м3",
                rostverkVolume, getPrice(params.getConcreteGrade())));

        // Арматура 14мм продольная: 4 прутка × длина ростверка, продаётся прутками 6м
        double rebar14TotalLength = totalLinear * REBAR14_ROWS;
        int rebar14Count = (int) Math.ceil(rebar14TotalLength / REBAR14_LENGTH);
        items.add(buildItem("Ростверк", "Арматура 14 мм", "шт",
                rebar14Count, getPrice("Арматура 14 мм")));

        // Арматура 8мм поперечная: хомуты с шагом 30 см
        double tieCount = Math.ceil(totalLinear / REBAR8_STEP);
        double rebar8TotalLength = tieCount * REBAR8_PIECE_LEN;
        // Продаётся в погонных метрах (цена за м)
        items.add(buildItem("Ростверк", "Арматура 8 мм", "п.м.",
                round2(rebar8TotalLength), getPrice("Арматура 8 мм")));

        // ===== ОПАЛУБКА =====

        // Доска 30×100×3000:
        // 2 стороны × (высота + запас) × длина × толщина доски
        double formworkVolume = round2(
                2 * (ROSTWERK_HEIGHT + FORMWORK_HEIGHT_MARGIN)
                        * totalLinear * FORMWORK_THICKNESS);
        items.add(buildItem("Опалубка", "Доска 30*100*3000", "м3",
                formworkVolume, getPrice("Доска 30*100*3000")));

        // Брус 50×50×3000 для крепежа:
        // (длина / шаг крепежа) × 2 стороны × длину бруска × сечение
        double brusCount = Math.ceil(totalLinear / BRUS_STEP);
        double brusVolume = round2(brusCount * 2 * BRUS_LENGTH * 0.05 * 0.05);
        items.add(buildItem("Опалубка", "Брус 50*50*3000", "м3",
                brusVolume, getPrice("Брус 50*50*3000")));

        return items;
    }

    // ===== Вспомогательные методы =====

    private BigDecimal getPrice(String materialName) {
        return materialRepository.findByName(materialName)
                .map(Material::getCurrentPrice)
                .orElse(BigDecimal.ZERO);
    }

    private CalculationResultItem buildItem(String section, String materialName,
                                            String unit, double quantity,
                                            BigDecimal unitPrice) {
        CalculationResultItem item = new CalculationResultItem();
        item.setSection(section);
        item.setMaterialName(materialName);
        item.setUnit(unit);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP));
        return item;
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
