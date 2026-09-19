package projeto.OButecoBack_End.model.repository.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import projeto.OButecoBack_End.model.entity.conversao.ConversoesEntity;

public interface ConversoesRepository extends JpaRepository<ConversoesEntity, Long> {
}
