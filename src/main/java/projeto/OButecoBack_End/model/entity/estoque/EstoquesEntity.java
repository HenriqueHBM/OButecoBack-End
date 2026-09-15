package projeto.OButecoBack_End.model.entity.estoque;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import projeto.OButecoBack_End.model.entity.conversao.ConversoesEntity;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;

import java.time.Instant;

@Getter
@Setter
@ToString

@Entity
@Table(name = "estoques")
public class EstoquesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_id_produto", nullable = false)
    private ProdutosEntity produtoEntity;

    @Column(name = "qtde_estoque", nullable = true)
    private double qntdEstoque;

    @ManyToOne
    @JoinColumn(name = "fk_id_conversao", nullable = false)
    private ConversoesEntity conversoesEntity;

    @Column(name = "local", nullable = true)
    private String local;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant dataCriacao;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant dataAtualizado;
}
