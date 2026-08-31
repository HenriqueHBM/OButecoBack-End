package projeto.OButecoBack_End.model.repository.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.OButecoBack_End.model.entity.produto.CategoriasEntity;

public interface CategoriasRepository extends JpaRepository<CategoriasEntity, Long> {
}
