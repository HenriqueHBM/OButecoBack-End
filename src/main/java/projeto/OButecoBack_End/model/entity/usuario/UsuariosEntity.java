package projeto.OButecoBack_End.model.entity.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import projeto.OButecoBack_End.model.Enum.CargoEnum;
import projeto.OButecoBack_End.model.Enum.EStatus;

import java.sql.Timestamp;

@Getter
@Setter
@ToString

@Entity
@Table(name = "usuarios")
public class UsuariosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "usuario", nullable = false, unique = true)
    private String usuario;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EStatus status;

    @Column(name = "cargo")
    private CargoEnum cargo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private Timestamp updated_at;

    @Column(name = "deleted_at", nullable = true)
    private Timestamp deletedAt;
}
