package projeto.OButecoBack_End.model.entity.produto;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@ToString

@Entity
@Table(name = "categorias")
public class CategoriasEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "categoria",  nullable = false)
    private String categoria;

    @Column(name = "deleted_at", nullable = true)
    private Timestamp deletedAt;

}
