package projeto.OButecoBack_End.model.repository.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.OButecoBack_End.model.entity.produto.InsumosProdutoEntity;

public interface InsumosProdutoRepository extends JpaRepository<InsumosProdutoEntity, Long> {
}
