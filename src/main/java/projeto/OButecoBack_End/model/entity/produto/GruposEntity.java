package projeto.OButecoBack_End.model.entity.produto;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString

@Entity
@Table(name = "grupos")
public class GruposEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "descricao")
    private String descricao;
}
