package projeto.OButecoBack_End.model.entity.estoque;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import projeto.OButecoBack_End.model.entity.conversao.ConversoesEntity;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;
import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;

import java.time.Instant;

@Getter
@Setter
@ToString

@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacoesEstoqueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_id_estoque", nullable = false)
    private EstoquesEntity estoqueEntity;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "qtde", nullable = false)
    private double quantidade;

    @Column(name = "valor_unitario", nullable = false)
    private double valorUnitario;

    @Column(name = "valor_total")
    private double valorTotal;

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

    @ManyToOne
    @JoinColumn(name = "fk_id_produto")
    private ProdutosEntity produtosEntity;
}
