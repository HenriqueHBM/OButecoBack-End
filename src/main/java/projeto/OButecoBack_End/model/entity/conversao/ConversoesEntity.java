package projeto.OButecoBack_End.model.entity.conversao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString

@Entity
@Table(name = "conversoes")
public class ConversoesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversao")
    private String nome;

    @Column(name = "nomenclatura")
    private String nomenclatura;

    @Column(name = "fator_base", precision = 10, scale = 4)
    private BigDecimal fatorBase;
}
