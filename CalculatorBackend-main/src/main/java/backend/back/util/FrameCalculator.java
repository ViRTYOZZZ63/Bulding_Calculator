package backend.back.util;



import backend.back.dto.request.FrameParamsRequest;
import backend.back.dto.request.OpeningDto;
import backend.back.entity.CalculationResultItem;
import backend.back.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import backend.back.entity.Material;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Калькулятор каркасного дома.
 * Алгоритм взят из файла РАСЧЕТ_КАРКАСА.xlsx, лист "алгоритм расчета каркаса"
 *
 * Константы по умолчанию (из листа "примечание"):
 * - Длина доски для стоек: 3 м
 * - Толщина доски: 50 мм = 0.05 м
 * - Шаг стоек: 600 мм = 0.6 м
 * - Длина доски для балок перекрытия: 6 м
 * - Кол-во балок перекрытия на 1 м² основания: 0.7 шт
 */
@Component
@RequiredArgsConstructor
public class FrameCalculator {

    private static final double STUD_STEP = 0.6;           // шаг стоек, м
    private static final double BOARD_THICKNESS = 0.05;     // толщина доски, м
    private static final double WALL_BOARD_LENGTH = 3.0;    // длина доски для стен, м
    private static final double CEILING_BOARD_LENGTH = 6.0; // длина доски для перекрытий, м
    private static final double BEAMS_PER_SQM = 0.7;        // балок на 1 м² основания
    private static final double OSB_WASTE = 1.15;           // запас 15%
    private static final double MEMBRANE_WASTE = 1.15;      // запас 15%
    private static final double INSULATION_WASTE = 1.10;    // запас 10%

    private final MaterialRepository materialRepository;

    /**
     * Выполняет расчёт каркаса по заданным параметрам.
     *
     * @param params входные параметры из запроса
     * @return список строк результата расчёта (материал, кол-во, цена)
     */
    public List<CalculationResultItem> calculate(FrameParamsRequest params) {
        List<CalculationResultItem> results = new ArrayList<>();

        // Расчёт по каждому этажу отдельно
        for (int floor = 1; floor <= params.getFloors(); floor++) {
            FrameParamsRequest.FloorParams fp = params.getFloorParams(floor);
            results.addAll(calculateFloor(floor, fp, params.getFloors()));
        }

        return results;
    }

    private List<CalculationResultItem> calculateFloor(int floorNumber,
                                                       FrameParamsRequest.FloorParams fp,
                                                       int totalFloors) {
        List<CalculationResultItem> items = new ArrayList<>();
        String floorLabel = floorNumber + " этаж";

        // ===== ВНЕШНИЕ СТЕНЫ =====
        double extWallArea = fp.getPerimeter() * fp.getFloorHeight();

        // Площадь проёмов (окна + внешние двери)
        double openingsArea = calcOpeningsArea(fp.getWindows()) + calcOpeningsArea(fp.getExternalDoors());

        // Стойки: периметр / шаг + 1
        int extStudCount = (int) Math.ceil(fp.getPerimeter() / STUD_STEP) + 1;
        // Обвязка: периметр × 2 (верх + низ) / длину доски
        int extBaseCount = (int) Math.ceil(fp.getPerimeter() * 2 / WALL_BOARD_LENGTH);
        // Доски на проёмы: периметр проёмов / длину доски
        double openingPerimeter = calcOpeningsPerimeter(fp.getWindows())
                + calcOpeningsPerimeter(fp.getExternalDoors());
        int extOpeningCount = (int) Math.ceil(openingPerimeter / WALL_BOARD_LENGTH);
        int extBoardCount = extStudCount + extBaseCount + extOpeningCount;

        // Ширина доски = толщина внешней стены (мм -> м)
        double extBoardWidthM = fp.getExtWallThickness() / 1000.0;
        double extBoardVolume = round2(extBoardCount * extBoardWidthM * WALL_BOARD_LENGTH * BOARD_THICKNESS);

        // Доска для внешних стен: 50 × толщина_стены × 3000
        String extBoardName = "Доска 50*" + fp.getExtWallThickness() + "*3000";
        items.add(buildItem("Внешние стены / " + floorLabel, extBoardName, "м3",
                extBoardVolume, getPrice(extBoardName)));

        // ОСБ для внешних стен: площадь × 2 (обе стороны) × 15%
        double extOsbArea = round2(extWallArea * 2 * OSB_WASTE);
        items.add(buildItem("Внешние стены / " + floorLabel, fp.getExtOsb(), "м2",
                extOsbArea, getPrice(fp.getExtOsb())));

        // Парогидроизоляция: площадь × 15%
        double extVaporArea = round2(extWallArea * MEMBRANE_WASTE);
        items.add(buildItem("Внешние стены / " + floorLabel, fp.getExtVapor(), "м2",
                extVaporArea, getPrice(fp.getExtVapor())));

        // Ветрозащита: площадь × 15%
        double extWindArea = round2(extWallArea * MEMBRANE_WASTE);
        items.add(buildItem("Внешние стены / " + floorLabel, fp.getExtWindBarrier(), "м2",
                extWindArea, getPrice(fp.getExtWindBarrier())));

        // Утеплитель: (площадь - проёмы) × 10%, объём = площадь × толщина
        double extInsulationArea = round2((extWallArea - openingsArea) * INSULATION_WASTE);
        double extInsulationVol = round2(extInsulationArea * extBoardWidthM);
        items.add(buildItem("Внешние стены / " + floorLabel, fp.getExtInsulation(), "м3",
                extInsulationVol, getPrice(fp.getExtInsulation())));

        // ===== ВНУТРЕННИЕ СТЕНЫ =====
        if (fp.getInnerWallLength() != null && fp.getInnerWallLength() > 0) {
            double intWallArea = fp.getInnerWallLength() * fp.getFloorHeight();
            double intBoardWidthM = fp.getIntWallThickness() / 1000.0;

            int intStudCount = (int) Math.ceil(fp.getInnerWallLength() / STUD_STEP) + 1;
            double intDoorPerimeter = calcOpeningsPerimeter(fp.getInternalDoors());
            int intOpeningCount = (int) Math.ceil(intDoorPerimeter / WALL_BOARD_LENGTH);
            int intBoardCount = intStudCount + intOpeningCount;

            double intBoardVolume = round2(intBoardCount * intBoardWidthM * WALL_BOARD_LENGTH * BOARD_THICKNESS);
            String intBoardName = "Доска 50*" + fp.getIntWallThickness() + "*3000";
            items.add(buildItem("Внутренние стены / " + floorLabel, intBoardName, "м3",
                    intBoardVolume, getPrice(intBoardName)));

            // ОСБ внутренних стен: площадь × 2 × 15%
            double intOsbArea = round2(intWallArea * 2 * OSB_WASTE);
            items.add(buildItem("Внутренние стены / " + floorLabel, fp.getIntOsb(), "м2",
                    intOsbArea, getPrice(fp.getIntOsb())));
        }

        // ===== ПЕРЕКРЫТИЯ =====
        if (fp.getCeilingThickness() != null) {
            double floorArea = fp.getFoundationArea();
            double ceilBoardWidthM = fp.getCeilingThickness() / 1000.0;

            // Балки перекрытия
            int beamCount = (int) Math.ceil(floorArea * BEAMS_PER_SQM);
            double beamVolume = round2(beamCount * ceilBoardWidthM * CEILING_BOARD_LENGTH * BOARD_THICKNESS);
            String ceilBoardName = "Доска 50*" + fp.getCeilingThickness() + "*6000";
            items.add(buildItem("Перекрытия / " + floorLabel, ceilBoardName, "м3",
                    beamVolume, getPrice(ceilBoardName)));

            // ОСБ перекрытий:
            // 1й этаж = площадь × 2 (пол+потолок) × 2 (с 2-х сторон) × 15%
            // 2й+ этаж = площадь × 1 (только потолок) × 2 (с 2-х сторон) × 15%
            double ceilLayers = (floorNumber == 1) ? 2.0 : 1.0;
            double ceilOsbArea = round2(floorArea * ceilLayers * 2 * OSB_WASTE);
            items.add(buildItem("Перекрытия / " + floorLabel, fp.getCeilingOsb(), "м2",
                    ceilOsbArea, getPrice(fp.getCeilingOsb())));

            // Пароизоляция перекрытия: площадь × 15%
            double ceilVaporArea = round2(floorArea * MEMBRANE_WASTE);
            items.add(buildItem("Перекрытия / " + floorLabel, fp.getCeilingVapor(), "м2",
                    ceilVaporArea, getPrice(fp.getCeilingVapor())));

            // Ветрозащита перекрытия: площадь × 15%
            double ceilWindArea = round2(floorArea * MEMBRANE_WASTE);
            items.add(buildItem("Перекрытия / " + floorLabel, fp.getCeilingWindBarrier(), "м2",
                    ceilWindArea, getPrice(fp.getCeilingWindBarrier())));

            // Утеплитель перекрытий:
            // 1й этаж = (площадь × 2) × 10% — пол и потолок
            // 2й+ этаж = (площадь × 1) × 10% — только потолок
            double insulLayers = (floorNumber == 1) ? 2.0 : 1.0;
            double ceilInsulArea = round2(floorArea * insulLayers * INSULATION_WASTE);
            double ceilInsulVol = round2(ceilInsulArea * ceilBoardWidthM);
            items.add(buildItem("Перекрытия / " + floorLabel, fp.getCeilingInsulation(), "м3",
                    ceilInsulVol, getPrice(fp.getCeilingInsulation())));
        }

        return items;
    }

    // ===== Вспомогательные методы =====

    private double calcOpeningsArea(List<OpeningDto> openings) {
        if (openings == null) return 0;
        return openings.stream()
                .mapToDouble(o -> o.getHeight() * o.getWidth() * o.getQuantity())
                .sum();
    }

    private double calcOpeningsPerimeter(List<OpeningDto> openings) {
        if (openings == null) return 0;
        return openings.stream()
                .mapToDouble(o -> 2 * (o.getHeight() + o.getWidth()) * o.getQuantity())
                .sum();
    }

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
