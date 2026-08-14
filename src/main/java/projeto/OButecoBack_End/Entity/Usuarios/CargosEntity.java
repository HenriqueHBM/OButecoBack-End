package projeto.OButecoBack_End.Entity.Usuarios;

import jakarta.persistence.*;

@Entity
@Table(name = "cargos")
public class CargosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cargo")
    private String cargo;
}
