package projeto.OButecoBack_End.model.service.estoque;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.controller.estoque.dto.EstoquesRequest;
import projeto.OButecoBack_End.model.entity.conversao.ConversoesEntity;
import projeto.OButecoBack_End.model.entity.estoque.EstoquesEntity;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;
import projeto.OButecoBack_End.model.repository.estoque.ConversoesRepository;
import projeto.OButecoBack_End.model.repository.estoque.EstoquesRepository;
import projeto.OButecoBack_End.model.repository.produto.ProdutosRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EstoquesService {

    private final EstoquesRepository estoquesRepository;
    private final ProdutosRepository produtosRepository;
    private final ConversoesRepository conversoesRepository;

    public EstoquesService(EstoquesRepository estoquesRepository, ProdutosRepository produtosRepository, ConversoesRepository conversoesRepository) {
        this.estoquesRepository = estoquesRepository;
        this.produtosRepository = produtosRepository;
        this.conversoesRepository = conversoesRepository;
    }

    public EstoquesEntity buscarEstoquePorId(Long id){
        return this.estoquesRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Estoque nao encontrado com id.")
        );
    }

    @Transactional
    public EstoquesEntity salvarEstoque(EstoquesRequest estoquesRequest){
        ProdutosEntity produto = produtosRepository.findById(estoquesRequest.fk_id_produto()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Produto nao encontrado com id.")
        );

        ConversoesEntity conversao = conversoesRepository.findById(estoquesRequest.fk_id_conversao()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Conversao nao encontrada com id")
        );

        EstoquesEntity estoquesEntity = new EstoquesEntity();

        estoquesEntity.setProdutosEntity(produto);
        estoquesEntity.setQntdEstoque(BigDecimal.valueOf(estoquesRequest.qtdeEstoque()));
        estoquesEntity.setConversoesEntity(conversao);
        estoquesEntity.setLocal(estoquesRequest.local());

        return estoquesRepository.save(estoquesEntity);
    }

    public List<EstoquesEntity> listarEstoques() {
        return estoquesRepository.findAll();
    }


}
