package projeto.OButecoBack_End.model.entity.produto;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import projeto.OButecoBack_End.model.entity.estoque.EstoquesEntity;
import projeto.OButecoBack_End.model.Enum.EStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString

@Entity
@Table(name = "produtos")
public class ProdutosEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO )
    private Long id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ATIVO'")
    private EStatus status =  EStatus.ATIVO;

    @ManyToOne
    @JoinColumn(name = "fk_id_categoria")
    private CategoriasEntity categoriaEntity;

    @ManyToOne
    @JoinColumn(name = "fk_id_grupo")
    private GruposEntity grupoEntity;

    @OneToMany(mappedBy = "produtoEntity", cascade = CascadeType.ALL)
    private List<ProdutosEntity> insumos = new ArrayList<>();

    @Column(name = "preco_venda")
    private double precoVenda;

    @Column(name = "observacao")
    private String observacao;

    @OneToMany(mappedBy = "produtosEntity", cascade = CascadeType.ALL)
    private List<EstoquesEntity> estoqueEntities = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant dataCriacao;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant dataAtualizacao;

}
