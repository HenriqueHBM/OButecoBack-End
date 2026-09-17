package projeto.OButecoBack_End.model.entity.estoque;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import projeto.OButecoBack_End.model.entity.conversao.ConversoesEntity;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;
import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@ToString

@Entity
@Table(name = "movimentacoes_estoques")
public class MovimentacoesEstoqueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_id_estoque", nullable = false)
    private EstoquesEntity estoqueEntity;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "qtde", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @ManyToOne
    @JoinColumn(name = "fk_id_usuario", nullable = false)
    private UsuariosEntity usuarioEntity;

    @ManyToOne
    @JoinColumn(name = "fk_id_conversao",nullable = false)
    private ConversoesEntity conversoesEntity;

    @CreationTimestamp
    @Column(name = "data_movimentacao")
    private Instant dataMovimentacao;

    @Column(name = "observacao", nullable = true)
    private String observacao;

    @Column(name = "qtde_conversao", precision = 1, scale = 2, nullable = true)
    private BigDecimal qtdeConversao;

    @ManyToOne
    @JoinColumn(name = "fk_id_produto")
    private ProdutosEntity produtosEntity;
}
