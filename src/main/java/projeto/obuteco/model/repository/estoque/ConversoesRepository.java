package projeto.obuteco.model.repository.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.obuteco.model.entity.conversao.ConversoesEntity;

public interface ConversoesRepository extends JpaRepository<ConversoesEntity, Long> {
}
