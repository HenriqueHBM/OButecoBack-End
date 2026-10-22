package projeto.obuteco.model.service.estoque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.estoque.dto.EstoquesRequest;
import projeto.obuteco.model.entity.conversao.ConversoesEntity;
import projeto.obuteco.model.entity.estoque.EstoquesEntity;
import projeto.obuteco.model.entity.produto.ProdutosEntity;
import projeto.obuteco.model.enums.CategoriaEnum;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.enums.GrupoEnum;
import projeto.obuteco.model.repository.estoque.ConversoesRepository;
import projeto.obuteco.model.repository.estoque.EstoquesRepository;
import projeto.obuteco.model.service.produto.ProdutosService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {
    @InjectMocks
    EstoquesService estoquesService;

    @Mock
    EstoquesRepository estoquesRepository;
    @Mock
    ConversoesRepository conversaoRepository;
    @Mock
    ProdutosService produtosService;

    @Captor
    ArgumentCaptor<EstoquesEntity> captorEstoque;

    ProdutosEntity produto = new ProdutosEntity(
            1L, "Espetinho", EStatus.ATIVO, CategoriaEnum.NORMAL,
            GrupoEnum.COMIDA, BigDecimal.valueOf(9.99), "Teste Prod");

    ConversoesEntity conversao = new ConversoesEntity(
            1L, "Quilograma", "KG", BigDecimal.valueOf(1));

    EstoquesEntity estoque = new EstoquesEntity(
            1L, produto, BigDecimal.valueOf(10), conversao, "Prateleira");

    EstoquesRequest request = new EstoquesRequest(1L, 10.0, 1L, "Prateleira");

    // ---------- listar ----------
    @Test
    @DisplayName("Deve retornar a lista de estoques ao ter sucesso")
    void listarEstoquesComSucesso(){
        //Arrange
        when(estoquesRepository.findAll()).thenReturn(List.of(estoque));

        //Act
        var resultado = estoquesService.listarEstoques();

        //Assert
        assertFalse(resultado.isEmpty());
        assertSame(estoque, resultado.get(0));
        verify(estoquesRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao ha estoques")
    void listarEstoquesVazio(){
        //Arrange
        when(estoquesRepository.findAll()).thenReturn(List.of());

        //Act
        var resultado = estoquesService.listarEstoques();

        //Assert
        assertTrue(resultado.isEmpty());
        verify(estoquesRepository).findAll();
    }

    // ---------- buscar estoque ----------
    @Test
    @DisplayName("Deve retornar o estoque quando o ID existir")
    void buscarEstoquePorIdComSucesso(){
        //Arrange
        when(estoquesRepository.findById(1L)).thenReturn(Optional.of(estoque));

        //Act
        var busca = estoquesService.buscarEstoquePorId(1L);

        //Assert
        assertSame(estoque, busca);
        assertEquals(1L, busca.getId());
        verify(estoquesRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar 404 quando o estoque nao existe")
    void buscarEstoquePorIdNaoEncontrado(){
        //Arrange
        when(estoquesRepository.findById(1L)).thenReturn(Optional.empty());

        //Act
        var excecao = assertThrows(ResponseStatusException.class,
                () -> estoquesService.buscarEstoquePorId(1L));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        verify(estoquesRepository).findById(1L);
    }

    // ---------- buscar conversao ----------
    @Test
    @DisplayName("Deve retornar a conversao quando o ID existir")
    void buscarConversaoPorIdComSucesso(){
        //Arrange
        when(conversaoRepository.findById(1L)).thenReturn(Optional.of(conversao));

        //Act
        var busca = estoquesService.buscarConversaoPorId(1L);

        //Assert
        assertSame(conversao, busca);
        verify(conversaoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar 404 quando conversao nao existe")
    void buscarConversaoPorIdNaoEncontrado(){
        //Arrange
        when(conversaoRepository.findById(1L)).thenReturn(Optional.empty());

        //Act
        var excecao = assertThrows(ResponseStatusException.class,
                () -> estoquesService.buscarConversaoPorId(1L));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        verify(conversaoRepository).findById(1L);
    }

    // ---------- salvar ----------
    @Test
    @DisplayName("Deve retornar o estoque salvo ao salvar estoque")
    void salvarEstoque(){
        //Arrange
        when(produtosService.buscarProdutoPorId(1L)).thenReturn(produto);
        when(conversaoRepository.findById(1L)).thenReturn(Optional.of(conversao));
        when(estoquesRepository.save(any(EstoquesEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        var resultado = estoquesService.salvarEstoque(request);

        //Assert
        verify(estoquesRepository).save(captorEstoque.capture());
        EstoquesEntity salvo = captorEstoque.getValue();
        assertSame(produto, salvo.getProdutosEntity());
        assertSame(conversao, salvo.getConversoesEntity());
        assertEquals(0, BigDecimal.valueOf(10.0).compareTo(salvo.getQntdEstoque()));
        assertEquals("Prateleira", salvo.getLocal());
        assertSame(salvo, resultado);
    }

    @Test
    @DisplayName("Deve retornar 404 ao nao encontrar o produto e nao salvar nada")
    void salvarEstoqueProdutoNaoEncontrado(){
        //Arrange
        when(produtosService.buscarProdutoPorId(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        //Act
        var excecao = assertThrows(ResponseStatusException.class,
                () -> estoquesService.salvarEstoque(request));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        verify(conversaoRepository, never()).findById(any());
        verify(estoquesRepository, never()).save(any(EstoquesEntity.class));
    }

    @Test
    @DisplayName("Deve retornar 404 ao nao encontrar a conversao e nao salvar nada")
    void salvarEstoqueConversaoNaoEncontrada(){
        //Arrange
        when(produtosService.buscarProdutoPorId(1L)).thenReturn(produto);
        when(conversaoRepository.findById(1L)).thenReturn(Optional.empty());

        //Act
        var excecao = assertThrows(ResponseStatusException.class,
                () -> estoquesService.salvarEstoque(request));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        verify(estoquesRepository, never()).save(any(EstoquesEntity.class));
    }
}