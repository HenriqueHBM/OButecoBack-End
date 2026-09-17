package projeto.OButecoBack_End.model.repository.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.OButecoBack_End.model.entity.produto.InsumosProdutoEntity;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;

import java.util.List;

public interface InsumosProdutoRepository extends JpaRepository<InsumosProdutoEntity, Long> {
    List<InsumosProdutoEntity> findAllByProdutoEntity(ProdutosEntity produto);

}
