package backend.back.entity;

import backend.back.entity.enums.ElementType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "calculation_elements")
@Data
@NoArgsConstructor
public class CalculationElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculation_id", nullable = false)
    private Calculation calculation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ElementType elementType;    // FRAME, FOUNDATION

    // Входные параметры храним как JSONB
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> inputParams;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "calculationElement", cascade = CascadeType.ALL)
    private List<CalculationResultItem> resultItems = new ArrayList<>();
}
