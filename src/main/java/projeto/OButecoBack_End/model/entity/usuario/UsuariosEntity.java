package projeto.OButecoBack_End.model.entity.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@ToString

@Entity
@Table(name = "usuarios")
public class UsuariosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "usuario", nullable = false, unique = true)
    private String usuario;

    @Column(name = "senha", nullable = false)
    private String senha;

    @ManyToOne
    @JoinColumn(name = "id_cargo", nullable = false)
    private CargosEntity cargosEntity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private Timestamp updated_at;

    @Column(name = "deleted_at", nullable = true)
    private Timestamp deletedAt;
}
