package projeto.obuteco.model.service.produto;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.produto.dto.ProdutoRequest;
import projeto.obuteco.model.entity.conversao.ConversoesEntity;
import projeto.obuteco.model.entity.produto.InsumosProdutoEntity;
import projeto.obuteco.model.enums.CategoriaEnum;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.enums.GrupoEnum;
import projeto.obuteco.model.entity.produto.ProdutosEntity;
import projeto.obuteco.model.repository.produto.ProdutosRepository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// @SpringBootTest
@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {
    @Mock
    private ProdutosRepository produtosRepository;

    @InjectMocks
    private ProdutosService produtosService;

    @Captor
    private ArgumentCaptor<ProdutosEntity> produtoCaptor;

    ProdutosEntity produto = new ProdutosEntity(
            1L,
            "Espetinho",
            EStatus.ATIVO,
            CategoriaEnum.NORMAL,
            GrupoEnum.COMIDA,
            BigDecimal.valueOf(9.99),
            "Teste Prod"
    );

    ProdutosEntity insumo = new ProdutosEntity(
            2L,
            "Queijo",
            EStatus.ATIVO,
            CategoriaEnum.INSUMO,
            GrupoEnum.COMIDA,
            BigDecimal.valueOf(2.50),
            "Teste Insumo"
    );

    ConversoesEntity conversoes = new ConversoesEntity(
        1L,
        "Quilograma",
        "KG",
        BigDecimal.valueOf(1)
    );

    InsumosProdutoEntity prodInsumo = new InsumosProdutoEntity(
            1L,
            produto,
            insumo,
            BigDecimal.valueOf(200),
            conversoes
    );

    ProdutoRequest prodRequest = new ProdutoRequest(
            1L,
            "Espetinho de Frango",
            EStatus.ATIVO,
            CategoriaEnum.NORMAL,
            GrupoEnum.COMIDA,
            BigDecimal.valueOf(9.99),
            "Teste Prod",
            List.of()
    );

    ProdutoRequest prodInsumosRequest = new ProdutoRequest(
            1L,
            "Espetinho de Frango",
            EStatus.ATIVO,
            CategoriaEnum.PRODUTO_INSUMOS,
            GrupoEnum.INGREDIENTE,
            BigDecimal.valueOf(9.99),
            "Teste Prod",
            List.of(
                    insumosRequest = new ProdutoRequest.InsumosRequest(
                            2L,
                            BigDecimal.valueOf(200)
                    )
            )
    );

    ProdutoRequest.InsumosRequest insumosRequest = new ProdutoRequest.InsumosRequest(
            2L,
            BigDecimal.valueOf(200)
    );


    @Test
    @DisplayName("verifica se retorna produto")
    void buscarProdutoPorId(){
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(produto));

        //produtosService.buscarProdutoPorId(produto.getId());
        assertEquals(produto, produtosService.buscarProdutoPorId(1L));
        verify(produtosRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    @DisplayName("verifica caso nao acha produto")
    void buscarPorIdNaoEncontrado(){
        when(produtosRepository.findByIdAndDeletedAtIsNull(0L))
                .thenReturn(Optional.empty());

        var resp = assertThrows(ResponseStatusException.class,
                () -> produtosService.buscarProdutoPorId(0L));

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void listarProdutos(){
        when(produtosRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of(produto));

        assertEquals(List.of(produto), produtosService.listarProdutos());
    }

    @Test
    void listarVazioProdutos(){
        when(produtosRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of());

        assertEquals(List.of(), produtosService.listarProdutos());
    }

    @Test
    void deletarProduto(){
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(produto));

        long time_antes = System.currentTimeMillis();
        produtosService.deletarProduto(1L);
        long time_depois = System.currentTimeMillis();

        verify(produtosRepository).save(produtoCaptor.capture());
        var salvo = produtoCaptor.getValue();


        assertEquals(EStatus.INATIVO, salvo.getStatus());
        assertNotNull(salvo.getDeletedAt());

        assertTrue(salvo.getDeletedAt().getTime() >= time_antes
                && salvo.getDeletedAt().getTime() <= time_depois);
    }

    // |---- Verificacao da lista de insumo da funcao [aplicarInsumos]
    @Test
    @DisplayName("Quando o produto nao tem insumos")
    void aplicarSemInsumo(){
        produto.getInsumos().add(prodInsumo);
        produtosService.aplicarInsumos(produto, null);

        assertTrue(produto.getInsumos().isEmpty());
        verify(produtosRepository).flush();
        verify(produtosRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Quando o produto vem vazio a lista de insumos")
    void aplicarInsumosEmpty(){
        produtosService.aplicarInsumos(produto, List.of());

        assertTrue(produto.getInsumos().isEmpty());
        verify(produtosRepository, never()).findById(any());
    }

    // |---- Verificacao do flush da funcao [aplicarInsumos]
    @Test
    @DisplayName("produto novo, nao chama o flush")
    void produtoNovoNaoFazFlush(){
        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));

        produto.setId(null);
        produtosService.aplicarInsumos(produto, List.of(insumosRequest));

        verify(produtosRepository, never()).flush();

    }

    @Test
    @DisplayName("Produto existente chama flush antes de buscar o insumo")
    void produtoExistenteFazFlush(){
        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));

        produtosService.aplicarInsumos(produto, List.of(insumosRequest));

        InOrder ordem = inOrder(produtosRepository);
        ordem.verify(produtosRepository).flush();
        ordem.verify(produtosRepository).findById(2L);
    }

    @Test
    @DisplayName("Adiciona um insumo no produto")
    void aplicarUmInsumoProduto(){
        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));

        produtosService.aplicarInsumos(produto, List.of(insumosRequest));

        assertTrue(produto.getInsumos().size() > 0);
        InsumosProdutoEntity vinculo = produto.getInsumos().get(0);
        assertEquals(vinculo.getProdutosEntity(), produto);
        assertEquals(vinculo.getInsumos(), insumo);
        assertEquals(vinculo.getQtde(), prodInsumo.getQtde());
    }

    @Test
    @DisplayName("Adiciona varios insumos no produto")
    void aplicarVariosInsumosProduto(){
        ProdutosEntity molho = new ProdutosEntity(3L, "Molho vermelho", EStatus.ATIVO,
                CategoriaEnum.INSUMO, GrupoEnum.COMIDA, BigDecimal.valueOf(1.50), "Teste Insumo 2");
        var molhoRequest = new ProdutoRequest.InsumosRequest(3L, BigDecimal.valueOf(50));

        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));
        when(produtosRepository.findById(3L)).thenReturn(Optional.of(molho));

        produtosService.aplicarInsumos(produto, List.of(insumosRequest, molhoRequest));

        assertEquals(2, produto.getInsumos().size());
        assertSame(insumo, produto.getInsumos().get(0).getInsumos());
        assertSame(molho, produto.getInsumos().get(1).getInsumos());

    }

    @Test
    @DisplayName("Substitui o insumo antigo pelo novo")
    void substituirInsumoAntigo(){
        ProdutosEntity molho = new ProdutosEntity(3L, "Molho vermelho", EStatus.ATIVO,
                CategoriaEnum.INSUMO, GrupoEnum.COMIDA, BigDecimal.valueOf(1.50), "Teste Insumo 2");
        var molhoRequest = new ProdutoRequest.InsumosRequest(3L, BigDecimal.valueOf(50));

        produto.getInsumos().add(prodInsumo);

        when(produtosRepository.findById(3L)).thenReturn(Optional.of(molho));

        produtosService.aplicarInsumos(produto, List.of(molhoRequest));

        assertEquals(1, produto.getInsumos().size());
        assertNotSame(prodInsumo, produto.getInsumos().get(0));
        assertSame(molho, produto.getInsumos().get(0).getInsumos());
    }

    @Test
    @DisplayName("Insumo repetido retorna 400")
    void insumoRepetido(){
        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));

        var itens = List.of(insumosRequest, insumosRequest);

        var resp = assertThrows(ResponseStatusException.class,
                () -> produtosService.aplicarInsumos(produto, itens));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Insumo repetido na lista: 2", resp.getReason());
        verify(produtosRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Produto usando ele mesmo como insumo retorna 400")
    void aplicarProdutoComoProprioInsumo(){
        var proprioRequest = new ProdutoRequest.InsumosRequest(1L, BigDecimal.valueOf(1)); // 1L = id do produto

        var itens = List.of(proprioRequest);


        var resp = assertThrows(ResponseStatusException.class,
                () -> produtosService.aplicarInsumos(produto, itens));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Produto e Insumos nao podem ser o mesmo", resp.getReason());
        verify(produtosRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Insumo nao encontrado retorna 404")
    void aplicarInsumoNaoEncontrado(){
        var inexistenteRequest = new ProdutoRequest.InsumosRequest(99L, BigDecimal.valueOf(1));
        when(produtosRepository.findById(99L)).thenReturn(Optional.empty());

        var itens = List.of(inexistenteRequest);


        var resp = assertThrows(ResponseStatusException.class,
                () -> produtosService.aplicarInsumos(produto, itens));

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("Insumo não encontrado: 99", resp.getReason());
    }

    @Test
    @DisplayName("Insumo que nao e da categoria INSUMO retorna 400")
    void aplicarInsumoCategoriaErrada(){
        insumo.setCategoriaEnum(CategoriaEnum.NORMAL);
        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));

        var itens = List.of(insumosRequest);


        var resp = assertThrows(ResponseStatusException.class,
                () -> produtosService.aplicarInsumos(produto, itens));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Insumo precisa ser da categoria de [Insumo]", resp.getReason());
    }

    @Test
    @DisplayName("Insumo sem categoria retorna 400")
    void aplicarInsumoCategoriaNula(){
        insumo.setCategoriaEnum(null);
        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));

        var itens = List.of(insumosRequest);


        var resp = assertThrows(ResponseStatusException.class,
                () -> produtosService.aplicarInsumos(produto, itens));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Insumo precisa ser da categoria de [Insumo]", resp.getReason());
    }

    @Test
    @DisplayName("Insumo com status INATIVO retorna 400")
    void aplicarInsumoInativo(){
        insumo.setStatus(EStatus.INATIVO);
        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));

        var itens = List.of(insumosRequest);


        var resp = assertThrows(ResponseStatusException.class,
                () -> produtosService.aplicarInsumos(produto, itens));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Insumo inativo: Queijo", resp.getReason());
    }

    @Test
    @DisplayName("Insumo deletado (deletedAt preenchido) retorna 400")
    void aplicarInsumoDeletado(){
        insumo.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));

        var itens = List.of(insumosRequest);


        var resp = assertThrows(ResponseStatusException.class,
                () -> produtosService.aplicarInsumos(produto, itens));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Insumo inativo: Queijo", resp.getReason());
    }

    @Test
    @DisplayName("Erro no segundo insumo mantem o primeiro na lista (estado parcial)")
    void aplicarInsumoErroNoSegundoItem(){
        var inexistenteRequest = new ProdutoRequest.InsumosRequest(99L, BigDecimal.valueOf(1));
        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));
        when(produtosRepository.findById(99L)).thenReturn(Optional.empty());

        var itens = List.of(insumosRequest, inexistenteRequest);


        assertThrows(ResponseStatusException.class,
                () -> produtosService.aplicarInsumos(produto, itens));

        assertEquals(1, produto.getInsumos().size());
        assertSame(insumo, produto.getInsumos().get(0).getInsumos());
    }

    // |--------------- funcao [atualizarStatusProduto]

    @Test
    @DisplayName("Atualiza o status do produto encontrado e ativo")
    void atualizarStatusProdutoAtivo(){
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(produto));

        produtosService.atualizarStatusProduto(1L);

        assertEquals(EStatus.INATIVO, produto.getStatus());
        verify(produtosRepository).save(produto);
    }

    @Test
    @DisplayName("Atualiza o status do produto encontrado e inativo")
    void atualizarStatusProdutoInativo(){
        produto.setStatus(EStatus.INATIVO);
        produto.setDeletedAt(new Timestamp(System.currentTimeMillis()));

        when(produtosRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(produto));

        produtosService.atualizarStatusProduto(1L);

        assertEquals(EStatus.ATIVO, produto.getStatus());
        assertEquals(null, produto.getDeletedAt());
        verify(produtosRepository).save(produto);
    }

    // | --------------------- [CADASTRAAR]

    @Test
    @DisplayName("Salva produto do tipo normal")
    void salvarProdutoNormal(){
        when(produtosRepository.save(any(ProdutosEntity.class)))
                .thenAnswer( i -> {
                    ProdutosEntity p = i.getArgument(0);
                    p.setId(1L);
                    return p;
                });

        var newProd = produtosService.salvarProduto(prodRequest);
        verify(produtosRepository).save(produtoCaptor.capture());

        var salvo = produtoCaptor.getValue();

        assertEquals("Espetinho de Frango", salvo.getNome());
        assertEquals(EStatus.ATIVO, salvo.getStatus());
        assertEquals(CategoriaEnum.NORMAL, salvo.getCategoriaEnum());
        assertEquals(GrupoEnum.COMIDA, salvo.getGrupo());
        assertEquals(BigDecimal.valueOf(9.99), salvo.getPrecoVenda());
        assertEquals("Teste Prod", salvo.getObservacao());
        assertTrue(salvo.getInsumosEntity().isEmpty());

        assertEquals(1L, newProd.getId());
    }

    @Test
    @DisplayName("Salva produto do tipo normal")
    void salvarProdutoComInsumo(){
        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));

        when(produtosRepository.save(any(ProdutosEntity.class)))
                .thenAnswer( i -> {
                    ProdutosEntity p = i.getArgument(0);
                    p.setId(1L);
                    return p;
                });

        var newProd = produtosService.salvarProduto(prodInsumosRequest);
        verify(produtosRepository).save(produtoCaptor.capture());

        var salvo = produtoCaptor.getValue();

        assertEquals("Espetinho de Frango", salvo.getNome());
        assertEquals(EStatus.ATIVO, salvo.getStatus());
        assertEquals(CategoriaEnum.PRODUTO_INSUMOS, salvo.getCategoriaEnum());
        assertEquals(GrupoEnum.INGREDIENTE, salvo.getGrupo());
        assertEquals(BigDecimal.valueOf(9.99), salvo.getPrecoVenda());
        assertEquals("Teste Prod", salvo.getObservacao());
        assertTrue(salvo.getInsumosEntity().isEmpty());

        assertEquals(1L, newProd.getId());
    }

    @ParameterizedTest
    @NullAndEmptySource //como a ultima excessa é null ou vazio, utilizamos essas duas anotation
    @DisplayName("Lança BAD_REQUEST ao salvar produto PRODUTO_INSUMOS sem insumos")
    void salvarProdutoComInsumoSemInsumo(List<ProdutoRequest.InsumosRequest> insumos) {
        var request = new ProdutoRequest(
                1L,
                "Espetinho de Frango",
                EStatus.ATIVO,
                CategoriaEnum.PRODUTO_INSUMOS,
                GrupoEnum.INGREDIENTE,
                BigDecimal.valueOf(9.99),
                "Teste Prod",
                insumos
        );

        var ex = assertThrows(ResponseStatusException.class,
                () -> produtosService.salvarProduto(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(ProdutosService.MSG_PRODUTO_INSUMO_SEM_INSUMO, ex.getReason());

        verify(produtosRepository, never()).save(any());
    }

    // |--------------- funcao [atualizarProduto]

    @Test
    @DisplayName("Atualiza produto para NORMAL e remove os insumos antigos")
    void atualizarProdutoNormal(){
        produto.getInsumos().add(prodInsumo);
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(produto));
        when(produtosRepository.save(produto)).thenReturn(produto);

        var atualizado = produtosService.atualizarProduto(1L, prodRequest);

        assertSame(produto, atualizado);
        assertEquals("Espetinho de Frango", atualizado.getNome());
        assertEquals(CategoriaEnum.NORMAL, atualizado.getCategoriaEnum());
        assertEquals(GrupoEnum.COMIDA, atualizado.getGrupo());
        assertEquals(BigDecimal.valueOf(9.99), atualizado.getPrecoVenda());
        assertEquals("Teste Prod", atualizado.getObservacao());
        assertTrue(atualizado.getInsumos().isEmpty());
        verify(produtosRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Atualiza produto para PRODUTO_INSUMOS aplicando os insumos")
    void atualizarProdutoComInsumos(){
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(produto));
        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));
        when(produtosRepository.save(produto)).thenReturn(produto);

        var atualizado = produtosService.atualizarProduto(1L, prodInsumosRequest);

        assertSame(produto, atualizado);
        assertEquals(CategoriaEnum.PRODUTO_INSUMOS, atualizado.getCategoriaEnum());
        assertEquals(GrupoEnum.INGREDIENTE, atualizado.getGrupo());
        assertEquals(1, atualizado.getInsumos().size());
        assertSame(insumo, atualizado.getInsumos().get(0).getInsumos());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Atualizar para PRODUTO_INSUMOS sem insumos retorna 400")
    void atualizarProdutoComInsumoSemInsumo(List<ProdutoRequest.InsumosRequest> insumos){
        var request = new ProdutoRequest(
                1L,
                "Espetinho de Frango",
                EStatus.ATIVO,
                CategoriaEnum.PRODUTO_INSUMOS,
                GrupoEnum.INGREDIENTE,
                BigDecimal.valueOf(9.99),
                "Teste Prod",
                insumos
        );
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(produto));

        var ex = assertThrows(ResponseStatusException.class,
                () -> produtosService.atualizarProduto(1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(ProdutosService.MSG_PRODUTO_INSUMO_SEM_INSUMO, ex.getReason());
        verify(produtosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Atualizar produto inexistente retorna 404")
    void atualizarProdutoNaoEncontrado(){
        when(produtosRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class,
                () -> produtosService.atualizarProduto(99L, prodRequest));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(produtosRepository, never()).save(any());
    }

// |--------------- funcao [atualizarProdutoParcial]

    @Test
    @DisplayName("PATCH com todos os campos nulos nao altera o produto")
    void atualizarParcialSemAlteracao(){
        var request = new ProdutoRequest(null, null, null, null, null, null, null, null);
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(produto));
        when(produtosRepository.save(produto)).thenReturn(produto);

        var atualizado = produtosService.atualizarProdutoParcial(1L, request);

        assertSame(produto, atualizado);
        assertEquals("Espetinho", atualizado.getNome());
        assertEquals(CategoriaEnum.NORMAL, atualizado.getCategoriaEnum());
        assertEquals(GrupoEnum.COMIDA, atualizado.getGrupo());
        assertEquals(BigDecimal.valueOf(9.99), atualizado.getPrecoVenda());
        assertEquals("Teste Prod", atualizado.getObservacao());
    }

    @Test
    @DisplayName("PATCH altera os campos enviados e limpa os insumos ao mudar para NORMAL")
    void atualizarParcialParaNormal(){
        produto.setCategoriaEnum(CategoriaEnum.PRODUTO_INSUMOS);
        produto.getInsumos().add(prodInsumo);
        var request = new ProdutoRequest(
                null,
                "Espetinho de Carne",
                null,
                CategoriaEnum.NORMAL,
                GrupoEnum.INGREDIENTE,
                BigDecimal.valueOf(12.50),
                "Obs alterada",
                null
        );
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(produto));
        when(produtosRepository.save(produto)).thenReturn(produto);

        var atualizado = produtosService.atualizarProdutoParcial(1L, request);

        assertEquals("Espetinho de Carne", atualizado.getNome());
        assertEquals(CategoriaEnum.NORMAL, atualizado.getCategoriaEnum());
        assertEquals(GrupoEnum.INGREDIENTE, atualizado.getGrupo());
        assertEquals(BigDecimal.valueOf(12.50), atualizado.getPrecoVenda());
        assertEquals("Obs alterada", atualizado.getObservacao());
        assertTrue(atualizado.getInsumos().isEmpty());
    }

    @Test
    @DisplayName("PATCH muda para PRODUTO_INSUMOS e aplica os insumos")
    void atualizarParcialParaProdutoInsumos(){
        var request = new ProdutoRequest(null, null, null, CategoriaEnum.PRODUTO_INSUMOS,
                null, null, null, List.of(insumosRequest));
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(produto));
        when(produtosRepository.findById(2L)).thenReturn(Optional.of(insumo));
        when(produtosRepository.save(produto)).thenReturn(produto);

        var atualizado = produtosService.atualizarProdutoParcial(1L, request);

        assertEquals("Espetinho", atualizado.getNome());
        assertEquals(CategoriaEnum.PRODUTO_INSUMOS, atualizado.getCategoriaEnum());
        assertEquals(1, atualizado.getInsumos().size());
        assertSame(insumo, atualizado.getInsumos().get(0).getInsumos());
    }

    @Test
    @DisplayName("PATCH sem categoria mantem os insumos do produto")
    void atualizarParcialMantemInsumos(){
        produto.setCategoriaEnum(CategoriaEnum.PRODUTO_INSUMOS);
        produto.getInsumos().add(prodInsumo);
        var request = new ProdutoRequest(null, "Espetinho de Frango", null, null,
                null, null, null, null);
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(produto));
        when(produtosRepository.save(produto)).thenReturn(produto);

        var atualizado = produtosService.atualizarProdutoParcial(1L, request);

        assertEquals("Espetinho de Frango", atualizado.getNome());
        assertEquals(1, atualizado.getInsumos().size());
        assertSame(prodInsumo, atualizado.getInsumos().get(0));
        verify(produtosRepository, never()).findById(any());
    }

    @Test
    @DisplayName("PATCH com insumos em produto que nao e PRODUTO_INSUMOS retorna 400")
    void atualizarParcialInsumosCategoriaErrada(){
        var request = new ProdutoRequest(null, null, null, null,
                null, null, null, List.of(insumosRequest));
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(produto));

        var ex = assertThrows(ResponseStatusException.class,
                () -> produtosService.atualizarProdutoParcial(1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Só é possível definir insumos em produto da categoria PRODUTO_INSUMOS", ex.getReason());
        verify(produtosRepository, never()).findById(any());
        verify(produtosRepository, never()).save(any());
    }

    @Test
    @DisplayName("PATCH para PRODUTO_INSUMOS sem insumos retorna 400")
    void atualizarParcialProdutoInsumosSemInsumo(){
        var request = new ProdutoRequest(null, null, null, CategoriaEnum.PRODUTO_INSUMOS,
                null, null, null, null);
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(produto));

        var ex = assertThrows(ResponseStatusException.class,
                () -> produtosService.atualizarProdutoParcial(1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(ProdutosService.MSG_PRODUTO_INSUMO_SEM_INSUMO, ex.getReason());
        verify(produtosRepository, never()).save(any());
    }
}
