package projeto.OButecoBack_End.model.repository.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.OButecoBack_End.model.entity.estoque.MovimentacoesEstoqueEntity;

import java.util.List;

public interface MovimentacoesEstoqueRepository extends JpaRepository<MovimentacoesEstoqueEntity, Long> {
    List<MovimentacoesEstoqueEntity> findByEstoqueEntityIdOrderByDataMovimentacaoDesc(Long idEstoque);
}
