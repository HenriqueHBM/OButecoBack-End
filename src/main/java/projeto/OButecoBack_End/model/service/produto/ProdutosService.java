package projeto.OButecoBack_End.model.service.produto;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.controller.produto.dto.ProdutoRequest;
import projeto.OButecoBack_End.model.Enum.CategoriaEnum;
import projeto.OButecoBack_End.model.Enum.EStatus;
import projeto.OButecoBack_End.model.entity.produto.InsumosProdutoEntity;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;
import projeto.OButecoBack_End.model.repository.produto.ProdutosRepository;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProdutosService {
    private final ProdutosRepository produtosRepository;

    //FIND Produto
    private ProdutosEntity buscarProdutoPorId(Long id){
        return this.produtosRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(
                    () -> {
                        log.warn("Produto não encontrado. ID: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Produto não encontrado");
                    }
            );
    }

    private void aplicarInsumos(ProdutosEntity produto, List<ProdutoRequest.InsumosRequest> itens){
        log.info("Aplicando insumos ao produto. Produto: {}, Quantidade de insumos: {}",
                produto.getId(),
                itens != null ? itens.size() : 0);

        //limpa os insumos
        produto.getInsumos().clear();

        if (produto.getId() != null) {//garante os delete antes das alteracoes/insert
            produtosRepository.flush();
        }

        if(itens == null || itens.isEmpty()) return; //pode ser um produto sem insumo por isso ja para de rodar a funcao

        Set<Long> jaAdicionados = new HashSet<>();
        for(var item : itens){
            //valida repeticao de insumos para o mesmo produto
            if(!jaAdicionados.add(item.insumoId())){
                log.warn("Insumo repetido na lista. Produto: {}, Insumo: {}", produto.getId(), item.insumoId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insumo repetido na lista: "+ item.insumoId());
            }
            //verifica se o insumo adicionado nao e o produto cadastrado (travando loop)
            if(produto.getId() != null && produto.getId().equals(item.insumoId())){
                log.warn("Produto tentando utilizar a si próprio como insumo. Produto: {}", produto.getId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto e Insumos nao podem ser o mesmo");
            }

            ProdutosEntity insumo = produtosRepository.findById(item.insumoId())
                .orElseThrow(
                        () -> {
                            log.warn("Insumo não encontrado com esse id: {}", item.insumoId());
                            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "Insumo não encontrado: "+ item.insumoId());
                        }
                );

            //caso o insumo escolhido nao seja da categoria de insumo
            if(insumo.getCategoriaEnum() != CategoriaEnum.INSUMO){
                log.warn("Insumo nao e do tipo [Insumo]: {}", item.insumoId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insumo precisa ser da categoria de [Insumo]");
            }

            //valida se o insumo nao foi apagado
            if (insumo.getDeletedAt() != null || insumo.getStatus() != EStatus.ATIVO) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Insumo inativo: " + insumo.getNome());
            }

            InsumosProdutoEntity vinculo = new InsumosProdutoEntity();
            vinculo.setProdutosEntity(produto);
            vinculo.setInsumos(insumo);
            vinculo.setQtde(item.qtde());
            produto.getInsumos().add(vinculo);
        }
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

        //caso for da categoria de "PRODUTO_INSUMOS"
        if(request.categoriaEnum() == CategoriaEnum.PRODUTO_INSUMOS){
            if(request.insumos() == null || request.insumos().isEmpty()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto com categoria de insumos precisa de ao menos um insumo");
            }
            this.aplicarInsumos(produto, request.insumos());
        }

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

        //caso for da categoria de "PRODUTO_INSUMOS"
        if(request.categoriaEnum() == CategoriaEnum.PRODUTO_INSUMOS){
            if(request.insumos() == null || request.insumos().isEmpty()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto com categoria de insumos precisa de ao menos um insumo");
            }
            this.aplicarInsumos(produto, request.insumos());
        }else{
            this.aplicarInsumos(produto, null);
        }

        log.info("Produto atualizado: {}", produto);
        return this.produtosRepository.save(produto);
    }

    //UPDATE Status
    @Transactional
    public ProdutosEntity atualizarStatusProduto(Long id){
        ProdutosEntity produto = this.buscarProdutoPorId(id);
        log.info("Produto antes da atualizacao: {}", produto);
        if (produto.getStatus() == EStatus.INATIVO) {
            produto.setStatus(EStatus.ATIVO);
            produto.setDeletedAt(null);
        } else {
            produto.setStatus(EStatus.INATIVO);
        }
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

            //caso seja alterado para um tipo que nao seja com insumos, ja limpa os antigos insumos
            if (request.categoriaEnum() != CategoriaEnum.PRODUTO_INSUMOS) {
                produto.getInsumos().clear();
            }
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

        if (request.insumos() != null) {
            CategoriaEnum categoria = produto.getCategoriaEnum();
            if (categoria != CategoriaEnum.PRODUTO_INSUMOS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Só é possível definir insumos em produto da categoria PRODUTO_INSUMOS");
            }
            aplicarInsumos(produto, request.insumos());
        }

        if (produto.getCategoriaEnum() == CategoriaEnum.PRODUTO_INSUMOS
                && produto.getInsumos().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Produto com categoria de insumos precisa de ao menos um insumo");
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
