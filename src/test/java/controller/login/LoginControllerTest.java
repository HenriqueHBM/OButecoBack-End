
package controller.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.login.LoginController;
import projeto.obuteco.controller.login.dto.LoginRequest;
import projeto.obuteco.controller.login.dto.LoginResponse;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.service.usuario.LoginService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private LoginService loginService;

    @InjectMocks
    private LoginController loginController;

    UsuariosEntity usuario = new UsuariosEntity(
            1L,
            "Ana",
            "Ana123",
            "Ana123",
            EStatus.ATIVO
    );

    LoginRequest loginRequest = new LoginRequest(
            "Ana123",
            "Ana123"
    );

    // |--------------- [LOGIN]

    @Test
    @DisplayName(" login que retorna 200 com a resposta do usuário")
    void loginSucesso() {
        when(loginService.autenticaoDoLogin(loginRequest))
                .thenReturn(usuario);

        var response = loginController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(LoginResponse.de(usuario), response.getBody());
        verify(loginService).autenticaoDoLogin(loginRequest);
    }


}
