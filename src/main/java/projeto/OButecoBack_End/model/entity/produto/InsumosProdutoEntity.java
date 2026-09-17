package projeto.OButecoBack_End.model.entity.produto;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString

@Entity
@Table(name = "insumos_produtos")
public class InsumosProdutoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_id_produto", nullable = false)
    private ProdutosEntity produtosEntity;

    @ManyToOne
    @JoinColumn(name = "fk_insumos_produto", nullable = false)
    private ProdutosEntity insumos;

    @Column(name = "qtde", precision = 10, scale = 2)
    private BigDecimal qtde;
}
