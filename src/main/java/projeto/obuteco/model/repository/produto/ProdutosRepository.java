package projeto.obuteco.model.repository.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.obuteco.model.entity.produto.ProdutosEntity;

import java.util.List;
import java.util.Optional;

public interface ProdutosRepository extends JpaRepository<ProdutosEntity, Long> {

    Optional<ProdutosEntity> findByIdAndDeletedAtIsNull(Long id);

    List<ProdutosEntity> findAllByDeletedAtIsNull();
}
