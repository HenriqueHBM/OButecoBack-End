package projeto.OButecoBack_End.model.repository.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.OButecoBack_End.model.entity.usuario.CargosEntity;

import java.util.Optional;

public interface CargosRepository extends JpaRepository <CargosEntity, Long> {
    Optional<CargosEntity> findByCargo(String cargo);
}
