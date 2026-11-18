package projeto.obuteco.model.service.usuario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.login.dto.LoginRequest;
import projeto.obuteco.controller.login.dto.LoginResponse;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.infra.spring.security.JwtService;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse autenticaoDoLogin(LoginRequest loginRequest) {
        log.info("Autenticando usuário: {}", loginRequest.usuario());
        try{
            var userPassword = new UsernamePasswordAuthenticationToken(loginRequest.usuario(), loginRequest.senha());
            var authentication = authenticationManager.authenticate(userPassword);

            var usuario = (UsuariosEntity) authentication.getPrincipal();
            log.info("Autenticação realizada com sucesso. Usuário: {}, ID: {}",
                    usuario.getUsuario(),
                    usuario.getId());

            // return jwtService.gerarToken(usuario);
            return new LoginResponse(
                    jwtService.gerarToken(usuario),
                    usuario.getId(),
                    usuario.getNome()
            );
        }catch(AuthenticationException err){
            log.warn("Falha no login: {}", loginRequest.usuario());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos");
        }
    }

}
