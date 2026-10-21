package projeto.obuteco.model.entity.produto;

import jakarta.persistence.*;
import lombok.*;
import projeto.obuteco.model.entity.conversao.ConversoesEntity;

import java.math.BigDecimal;

@Getter
@Setter
@ToString(exclude = {"produto", "insumo"})
@Entity
@Table(name = "insumos_produtos")
public class InsumosProdutoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_produto", nullable = false)
    private ProdutosEntity produtosEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_insumos_produto", nullable = false)
    private ProdutosEntity insumos;

    @Column(name = "qtde", precision = 10, scale = 2)
    private BigDecimal qtde;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_conversao")
    private ConversoesEntity conversoesEntity;

    public InsumosProdutoEntity(){}

    public InsumosProdutoEntity(Long id, ProdutosEntity produtosEntity, ProdutosEntity insumos, BigDecimal qtde, ConversoesEntity conversoesEntity) {
        this.id = id;
        this.produtosEntity = produtosEntity;
        this.insumos = insumos;
        this.qtde = qtde;
        this.conversoesEntity = conversoesEntity;
    }
}
