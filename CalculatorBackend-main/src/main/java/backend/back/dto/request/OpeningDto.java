package backend.back.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** Проём (окно или дверь) */
@Data
public class OpeningDto {

    @NotNull @Positive
    private Double height;     // высота, м

    @NotNull @Positive
    private Double width;      // ширина, м

    @NotNull @Positive
    private Integer quantity;  // количество, шт
}
