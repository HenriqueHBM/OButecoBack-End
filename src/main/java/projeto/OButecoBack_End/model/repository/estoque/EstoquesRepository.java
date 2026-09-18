package projeto.OButecoBack_End.model.repository.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.OButecoBack_End.model.entity.estoque.EstoquesEntity;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;

import java.util.Optional;

public interface EstoquesRepository extends JpaRepository<EstoquesEntity, Long> {
    Optional<EstoquesEntity> findByProdutosEntity(ProdutosEntity produto);
}
