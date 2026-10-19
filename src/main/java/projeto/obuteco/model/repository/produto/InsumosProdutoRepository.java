package projeto.obuteco.model.repository.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.obuteco.model.entity.produto.InsumosProdutoEntity;
import projeto.obuteco.model.entity.produto.ProdutosEntity;

import java.util.List;

public interface InsumosProdutoRepository extends JpaRepository<InsumosProdutoEntity, Long> {
    List<InsumosProdutoEntity> findAllByProdutosEntity(ProdutosEntity produto);

}
