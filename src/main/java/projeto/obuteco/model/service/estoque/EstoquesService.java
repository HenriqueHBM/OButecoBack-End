package projeto.obuteco.model.service.estoque;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.estoque.dto.EstoquesRequest;
import projeto.obuteco.model.entity.conversao.ConversoesEntity;
import projeto.obuteco.model.entity.estoque.EstoquesEntity;
import projeto.obuteco.model.entity.produto.ProdutosEntity;
import projeto.obuteco.model.repository.estoque.ConversoesRepository;
import projeto.obuteco.model.repository.estoque.EstoquesRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projeto.obuteco.model.service.produto.ProdutosService;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EstoquesService {
    private static final Logger log = LoggerFactory.getLogger(EstoquesService.class);

    private final ProdutosService produtosService;
    private final EstoquesRepository estoquesRepository;
    private final ConversoesRepository conversoesRepository;

    public EstoquesService(ProdutosService produtosService, EstoquesRepository estoquesRepository, ConversoesRepository conversoesRepository) {
        this.produtosService = produtosService;
        this.estoquesRepository = estoquesRepository;
        this.conversoesRepository = conversoesRepository;
    }

    public EstoquesEntity buscarEstoquePorId(Long id){
        log.info("Buscando estoque pelo ID: {}", id);

        return this.estoquesRepository.findById(id).orElseThrow(
                () -> {
                    log.warn("Estoque não encontrado. ID: {}", id);

                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Estoque nao encontrado com id."); }
        );
    }

    public ConversoesEntity buscarConversaoPorId(Long id){
        log.info("Buscando conversao pelo ID: {}", id);
        return this.conversoesRepository.findById(id).orElseThrow(
                () -> {
                    log.warn("Conversão não encontrada.ID: {}", id);

                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Conversão não encontrada com id");
                }
        );
    }


    @Transactional
    public EstoquesEntity salvarEstoque(EstoquesRequest estoquesRequest){
        log.info("Iniciando criação de estoque: {}", estoquesRequest);

        ProdutosEntity produto = produtosService.buscarProdutoPorId(estoquesRequest.fk_id_produto());

        ConversoesEntity conversao = buscarConversaoPorId(estoquesRequest.fk_id_conversao());

        EstoquesEntity estoquesEntity = new EstoquesEntity();

        estoquesEntity.setProdutosEntity(produto);
        estoquesEntity.setQntdEstoque(BigDecimal.valueOf(estoquesRequest.qtdeEstoque()));
        estoquesEntity.setConversoesEntity(conversao);
        estoquesEntity.setLocal(estoquesRequest.local());

        return estoquesRepository.save(estoquesEntity);
    }

    public List<EstoquesEntity> listarEstoques() {
        log.info("Buscando todos os estoques");

        return estoquesRepository.findAll();
    }


}
