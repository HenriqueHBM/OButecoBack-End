package projeto.OButecoBack_End.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString


@Entity
@Table(name = "cargos")
public class CargosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cargo")
    private String cargo;
}
