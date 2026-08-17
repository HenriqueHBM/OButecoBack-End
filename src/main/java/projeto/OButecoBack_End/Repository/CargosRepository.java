package projeto.OButecoBack_End.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.OButecoBack_End.Entity.CargosEntity;

import java.util.Optional;

public interface CargosRepository extends JpaRepository <CargosEntity, Long> {
    Optional<CargosEntity> findByCargo(String cargo);
}
