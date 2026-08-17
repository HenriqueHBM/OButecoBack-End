package projeto.OButecoBack_End.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projeto.OButecoBack_End.Entity.UsuariosEntity;

import java.util.List;
import java.util.Optional;

public interface UsuariosRepository extends JpaRepository<UsuariosEntity,Long> {
    List<UsuariosEntity> findAllByDeletedAtIsNull();
    Optional<UsuariosEntity> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT u FROM UsuariosEntity u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND u.deleted_at IS NULL")
    List<UsuariosEntity> findByNome(@Param("nome") String nome);

    Optional<UsuariosEntity> findByUsuarioAndDeletedAtIsNull(String usuario);
}
