package projeto.OButecoBack_End.model.entity.produto;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import projeto.OButecoBack_End.model.Enum.CategoriaEnum;
import projeto.OButecoBack_End.model.Enum.GrupoEnum;
import projeto.OButecoBack_End.model.entity.estoque.EstoquesEntity;
import projeto.OButecoBack_End.model.Enum.EStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString

@Entity
@Table(name = "produtos")
public class ProdutosEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ATIVO'")
    private EStatus status;

    @Column(name = "categoria", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private CategoriaEnum categoriaEnum;

    @Column(name = "grupo", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private GrupoEnum grupo;

    //@OneToMany(mappedBy = "produtoEntity", cascade = CascadeType.ALL)
    //private List<ProdutosEntity> insumos = new ArrayList<>();

    @Column(name = "preco_venda", precision = 10, scale = 2, nullable = true)
    private BigDecimal precoVenda;

    @Column(name = "observacao")
    private String observacao;

    @OneToMany(mappedBy = "produtosEntity", cascade = CascadeType.ALL)
    private List<EstoquesEntity> EstoquesEntity = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp dataCriacao;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp dataAtualizacao;

    @Column(name = "deleted_at", nullable = true)
    private Timestamp deletedAt;

    @ManyToMany
    @JoinTable(
            name = "insumos_produtos",
            joinColumns = @JoinColumn(name = "fk_id_produto"),
            inverseJoinColumns = @JoinColumn(name = "fk_insumos_produto")
    )
    private Set<ProdutosEntity> insumosEntity = new HashSet<>();

}
