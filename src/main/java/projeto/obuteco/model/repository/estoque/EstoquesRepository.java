package projeto.obuteco.model.repository.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.obuteco.model.entity.estoque.EstoquesEntity;
import projeto.obuteco.model.entity.produto.ProdutosEntity;

import java.util.Optional;

public interface EstoquesRepository extends JpaRepository<EstoquesEntity, Long> {
    Optional<EstoquesEntity> findByProdutosEntity(ProdutosEntity produto);
}
