package projeto.OButecoBack_End.model.service.produto;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.controller.produto.dto.ProdutoRequest;
import projeto.OButecoBack_End.model.Enum.EStatus;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;
import projeto.OButecoBack_End.model.repository.produto.ProdutosRepository;

import java.sql.Timestamp;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProdutosService {
    private final ProdutosRepository produtosRepository;

    //FIND Produto
    public ProdutosEntity buscarProdutoPorId(Long id){
        return this.produtosRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(
                    () -> {
                        log.warn("Produto com esse id não encontrado: ", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Produto não encontrado");
                    }
            );
    }

    //CREATE
    @Transactional
    public ProdutosEntity salvarProduto(ProdutoRequest request){
        ProdutosEntity produto = new ProdutosEntity();
        produto.setNome(request.nome());
        produto.setStatus(EStatus.ATIVO);

        produto.setCategoriaEnum(request.categoriaEnum());
        produto.setGrupo(request.grupoEnum());
        produto.setPrecoVenda(request.precoVenda());
        produto.setObservacao(request.observacao());
        produto.setDataCriacao(new Timestamp(System.currentTimeMillis()));

        log.info("Produto criado: {}", produto);
        return this.produtosRepository.save(produto);

    }

    //UPDATE Geral
    @Transactional
    public ProdutosEntity atualizarProduto(Long id, ProdutoRequest request){
        ProdutosEntity produto = this.buscarProdutoPorId(id);

        produto.setCategoriaEnum(request.categoriaEnum());
        produto.setNome(request.nome());
        produto.setGrupo(request.grupoEnum());
        produto.setPrecoVenda(request.precoVenda());
        produto.setObservacao(request.observacao());

        log.info("Produto atualizado: {}", produto);
        return this.produtosRepository.save(produto);
    }

    //UPDATE Status
    @Transactional
    public ProdutosEntity atualizarStatusProduto(Long id){
        ProdutosEntity produto = this.buscarProdutoPorId(id);
        log.info("Produto antes da atualizacao: {}", produto);
        produto.setStatus(produto.getStatus().equals(EStatus.ATIVO) ? EStatus.INATIVO : EStatus.ATIVO);
        log.info("Produto atualizado: {}", produto);
        return this.produtosRepository.save(produto);
    }

    //UPDATE em PATCH
    @Transactional
    public ProdutosEntity atualizarProdutoParcial(Long id, ProdutoRequest request){
        ProdutosEntity produto = this.buscarProdutoPorId(id);
        log.info("Produto antes da atualizacao: {}", produto);

        if(request.nome() != null){
            produto.setNome(request.nome());
        }

        if(request.categoriaEnum() != null){
            produto.setCategoriaEnum(request.categoriaEnum());
        }

        if(request.grupoEnum() != null){
            produto.setGrupo(request.grupoEnum());
        }

        if(request.precoVenda() != null){
            produto.setPrecoVenda(request.precoVenda());
        }
        if(request.observacao() != null){
            produto.setObservacao(request.observacao());
        }

        log.info("Dado do produto atualizado: {}", produto);
        return this.produtosRepository.save(produto);
    }

    //DELETE
    @Transactional
    public void deletarProduto(Long id){
        ProdutosEntity produto = this.buscarProdutoPorId(id);
        produto.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        produto.setStatus(EStatus.INATIVO);

        log.info("Produto deletado: {}", produto);
        this.produtosRepository.save(produto);
    }

    //READ/LIST
    public List<ProdutosEntity> listarProdutos(){
        return this.produtosRepository.findAllByDeletedAtIsNull();
    }
}
