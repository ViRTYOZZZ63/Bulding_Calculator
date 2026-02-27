package backend.back.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "calculation_result_items")
@Data
@NoArgsConstructor
public class CalculationResultItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculation_element_id", nullable = false)
    private CalculationElement calculationElement;

    private String materialName;
    private String unit;               // м², м³, шт и т.д.

    @Column(columnDefinition = "numeric(12,4)")
    private Double quantity;
    private BigDecimal unitPrice;      // цена на момент расчёта (снимок)
    private BigDecimal totalPrice;     // quantity * unitPrice
    private String section;            // "Внешние стены", "Перекрытия" и т.д.
}
