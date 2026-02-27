package backend.back.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * DTO входных параметров для расчёта каркаса.
 * Параметры соответствуют листу "исходные данные для стен" в РАСЧЕТ_КАРКАСА.xlsx
 */
@Data
public class FrameParamsRequest {

    /** Количество этажей */
    @NotNull
    @Min(1) @Max(5)
    private Integer floors;

    /** Параметры по каждому этажу (список размером = floors) */
    @NotEmpty
    @Valid
    private List<FloorParams> floorParamsList;

    public FloorParams getFloorParams(int floorNumber) {
        return floorParamsList.get(floorNumber - 1);
    }

    @Data
    public static class FloorParams {

        // ===== Коробка =====

        /** Высота этажа, м (не более 3 м) */
        @NotNull
        @DecimalMin("2.0") @DecimalMax("3.5")
        private Double floorHeight;

        /** Периметр внешних стен, м */
        @NotNull
        @Positive
        private Double perimeter;

        /** Площадь основания (фундамента), м² */
        @NotNull
        @Positive
        private Double foundationArea;

        /**
         * Толщина внешних стен, мм.
         * Выпадающий список: 100, 150, 200, 250
         * Влияет на ширину досок и толщину утеплителя.
         */
        @NotNull
        private ExtWallThickness extWallThickness;

        /** Длина внутренних стен, м */
        private Double innerWallLength;

        /**
         * Толщина внутренних стен, мм.
         * Выпадающий список: 100
         */
        private IntWallThickness intWallThickness;

        // ===== Окна и двери =====

        /** Оконные проёмы */
        @Valid
        private List<OpeningDto> windows;

        /** Дверные проёмы внешние */
        @Valid
        private List<OpeningDto> externalDoors;

        /** Дверные проёмы внутренние */
        @Valid
        private List<OpeningDto> internalDoors;

        // ===== Обшивка внешних стен =====

        /**
         * ОСБ для внешних стен.
         * Варианты: "OSB 9 мм", "OSB 10 мм", "OSB 15 мм", "OSB 18 мм"
         */
        private String extOsb;

        /**
         * Парогидроизоляция.
         * Варианты: "Ондутис", "Пароизоляция Axton (b)",
         *           "Пароизоляционная пленка Ютафол Н 96 Сильвер", "Пароизоляция В"
         */
        private String extVapor;

        /**
         * Ветрозащита.
         * Варианты: "Ветро-влагозащитная мембрана Brane А",
         *           "Паропроницаемая ветро-влагозащита A Optima",
         *           "Гидро-ветрозащита Тип А"
         */
        private String extWindBarrier;

        /**
         * Утеплитель для внешних стен.
         * Зависит от толщины стены:
         * 100мм → Кнауф/Технониколь/Эковер 100мм
         * 150мм → Эковер 150мм
         * 200мм → Эковер 200мм, Фасад 200мм
         * 250мм → Эковер 250мм
         */
        private String extInsulation;

        // ===== Обшивка внутренних стен =====

        /** ОСБ для внутренних стен */
        private String intOsb;

        // ===== Перекрытия =====

        /**
         * Толщина перекрытия, мм.
         * Выпадающий список: 200, 250
         * Влияет на ширину досок балок.
         * Для 1 этажа: 2 слоя (пол + потолок).
         * Для 2+ этажей: 1 слой (только потолок).
         */
        private CeilingThickness ceilingThickness;

        /** ОСБ для перекрытий */
        private String ceilingOsb;

        /** Парогидроизоляция перекрытий */
        private String ceilingVapor;

        /** Ветрозащита перекрытий */
        private String ceilingWindBarrier;

        /** Утеплитель перекрытий */
        private String ceilingInsulation;

        // Геттеры для числовых значений enum

        public Integer getExtWallThickness() {
            return extWallThickness != null ? extWallThickness.getMm() : null;
        }

        public Integer getIntWallThickness() {
            return intWallThickness != null ? intWallThickness.getMm() : 100;
        }

        public Integer getCeilingThickness() {
            return ceilingThickness != null ? ceilingThickness.getMm() : null;
        }
    }

    // ===== Enum для выпадающих списков =====

    public enum ExtWallThickness {
        MM_100(100), MM_150(150), MM_200(200), MM_250(250);
        private final int mm;
        ExtWallThickness(int mm) { this.mm = mm; }
        public int getMm() { return mm; }
    }

    public enum IntWallThickness {
        MM_100(100);
        private final int mm;
        IntWallThickness(int mm) { this.mm = mm; }
        public int getMm() { return mm; }
    }

    public enum CeilingThickness {
        MM_200(200), MM_250(250);
        private final int mm;
        CeilingThickness(int mm) { this.mm = mm; }
        public int getMm() { return mm; }
    }
}
