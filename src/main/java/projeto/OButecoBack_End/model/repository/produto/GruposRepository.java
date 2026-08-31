package projeto.OButecoBack_End.model.repository.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.OButecoBack_End.model.entity.produto.GruposEntity;

public interface GruposRepository extends JpaRepository<GruposEntity, Long> {
}
