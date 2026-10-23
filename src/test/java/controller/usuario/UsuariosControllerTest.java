package controller.usuario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import projeto.obuteco.controller.endereco.EnderecoResponse;
import projeto.obuteco.controller.usuario.dto.UsuariosResponse;
import projeto.obuteco.controller.usuario.UsuariosController;
import projeto.obuteco.controller.usuario.dto.UsuariosRequest;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.enums.CargoEnum;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.service.endereco.EnderecoService;
import projeto.obuteco.model.service.usuario.UsuariosService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)

 class UsuariosControllerTest {
    @Mock
    private UsuariosService usuariosService;

    @Mock
    private EnderecoService enderecoService;

    @InjectMocks
    private UsuariosController usuariosController;

    UsuariosEntity usuario = new UsuariosEntity(
            1L,
            "Ana",
            "Ana123",
            "Ana123",
            EStatus.ATIVO
    );

    UsuariosRequest userRequest = new UsuariosRequest(
            1L,
            "Ana",
            "Ana123",
            "Ana123",
            CargoEnum.ADMIN,
            null,
            null,
            null
    );

    EnderecoResponse endereco = new EnderecoResponse("",
            "Rua Buteco",
            "casa",
            "Buteco",
            "Foz do Iguaçu",
            "PR",
            "PR");

    @Test
    @DisplayName("Cadastrar usuario retorna 201 com o usuario criado")
    void salvarUsuario(){
        when(usuariosService.salvarUsuario(userRequest)).thenReturn(usuario);

        var response = usuariosController.salvarUsuario(userRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(UsuariosResponse.de(usuario), response.getBody());
        verify(usuariosService).salvarUsuario(userRequest);
    }

    @Test
    @DisplayName("Buscar usuario retornando 200")
    void buscarUsuario(){
        when(usuariosService.buscarPorNome("Ana")).thenReturn(List.of(usuario));

        var response = usuariosController.buscarUsuarios("Ana");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(UsuariosResponse.de(usuario), response.getBody().get(0));

        verify(usuariosService).buscarPorNome("Ana");
    }

    @Test
    @DisplayName("Buscar usuario por id retorna 200")
    void buscarPorId(){
        when(usuariosService.buscarUsuarioPorId(1L)).thenReturn(usuario);

        var response = usuariosController.buscarPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(UsuariosResponse.de(usuario), response.getBody());
    }

    @Test
    @DisplayName("Buscar endereço por CEP retorna 200 com o endereço encontrado")
    void buscarCep(){
        when(enderecoService.buscarPorCep("12345678"))
                .thenReturn(endereco);

        var response = usuariosController.buscarCep("12345678");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(endereco, response.getBody());

        verify(enderecoService).buscarPorCep("12345678");
    }

    @Test
    @DisplayName("atualizar usuario retornando 200")
    void atualiazarUsuario(){
        when(usuariosService.atualizarUsuario(1L, userRequest)).thenReturn(usuario);

        var response = usuariosController.atualizarUsuario(1L, userRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(UsuariosResponse.de(usuario), response.getBody());
        verify(usuariosService).atualizarUsuario(1L, userRequest);
    }

    @Test
    @DisplayName("atualizar usuario parcialmente retorna 200")
    void atualizarusuarioParcial(){
        var request = new UsuariosRequest(null, "Luis", null, null,
                null, null, null, null);
        usuario.setNome("Luis");
        when(usuariosService.atualizarUsuarioParcial(1L, request)).thenReturn(usuario);

        var response = usuariosController.atualizarUsuarioParcial(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(UsuariosResponse.de(usuario), response.getBody());
        verify(usuariosService).atualizarUsuarioParcial(1L, request);
    }

    @Test
    @DisplayName("Alterar status do usuario retorna 200 com o usuario atualizado")
    void atualizarStatusUsuario(){
    usuario.setStatus(EStatus.INATIVO);
    when(usuariosService.atualizarStatusUsuario(1L)).thenReturn(usuario);

    var response = usuariosController.atualizarStatusUsuario(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(UsuariosResponse.de(usuario), response.getBody());
        verify(usuariosService).atualizarStatusUsuario(1L);
    }

    @Test
    @DisplayName("Deletar usuario retorna 204 sem corpo")
    void deletarUsuario(){
        var response = usuariosController.deletarUsuario(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(usuariosService).deletarUsuarioPorId(1L);
    }

    @Test
    @DisplayName("Listar usuario retorna 200")
    void listarUsuarios(){
       when(usuariosService.listarUsuarios()).thenReturn(List.of(usuario));

        var response = usuariosController.listarUsuarios();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(UsuariosResponse.de(usuario)), response.getBody());
    }
}
