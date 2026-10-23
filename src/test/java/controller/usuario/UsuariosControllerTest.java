package controller.usuario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import projeto.obuteco.controller.produto.dto.ProdutoResponse;
import projeto.obuteco.controller.usuario.UsuariosController;
import projeto.obuteco.controller.usuario.dto.UsuariosRequest;
import projeto.obuteco.controller.usuario.dto.UsuariosResponse;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.enums.CargoEnum;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.service.usuario.UsuariosService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)

 class UsuariosControllerTest {
    @Mock
    private UsuariosService usuariosService;

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
            1L, "Ana", "Ana123", "Ana123", CargoEnum.ADMIN, null, null, null
    );

    @Test
    @DisplayName("Cadastrar usuario retorna 201 com o usuario criado")
    void salvarUsuario(){
        UsuariosRequest request = new UsuariosRequest(
                null, "Ana", "Ana123", "Ana123",
                CargoEnum.ADMIN, null, null, null
        );

        when(usuariosService.salvarUsuario(request)).thenReturn(usuario);

        var response = usuariosController.salvarUsuario(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(UsuariosResponse.de(usuario), response.getBody());
        verify(usuariosService).salvarUsuario(request);
    }

}
