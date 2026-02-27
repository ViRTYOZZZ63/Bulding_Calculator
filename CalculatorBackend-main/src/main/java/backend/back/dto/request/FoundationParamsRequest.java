package backend.back.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO входных параметров для расчёта свайного фундамента (забивные сваи).
 * Параметры соответствуют листу "ЗАБИВНЫЕ СВАИ" в свайный_фундамент.xlsx
 *
 * Входные параметры берутся те же, что и для каркаса (периметр, длина внутренних стен)
 */
@Data
public class FoundationParamsRequest {

    /**
     * Периметр внешних стен, м.
     * Тот же параметр, что используется в расчёте каркаса.
     */
    @NotNull
    @Positive
    private Double externalPerimeter;

    /**
     * Длина внутренних стен, м.
     * Тот же параметр, что используется в расчёте каркаса.
     */
    @NotNull
    @Positive
    private Double innerWallLength;

    /**
     * Тип бетонных свай.
     * Выпадающий список из справочника:
     * "Бетонные сваи 150*150*3000" — 1500 руб/шт
     * "Бетонные сваи 200*200*3000" — 1900 руб/шт
     * "Бетонные сваи 300*300*3000" — 3383 руб/шт
     * "Бетонные сваи 300*300*4000" — 4346 руб/шт
     * "Бетонные сваи 300*300*5000" — 4953 руб/шт
     */
    @NotBlank
    private String pileType;

    /**
     * Марка бетона для ростверка.
     * Выпадающий список из справочника:
     * "М150(В10)" — 2760 руб/м3
     * "М200(В15)" — 2910 руб/м3
     * "М250(В20)" — 3100 руб/м3
     * "М300 (В22.5)" — 3160 руб/м3
     * "М350(В25)" — 3260 руб/м3
     * "М400(В30)" — 3460 руб/м3
     * ... и т.д.
     */
    @NotBlank
    private String concreteGrade;
}
