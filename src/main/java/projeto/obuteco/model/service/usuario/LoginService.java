package projeto.obuteco.model.service.usuario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.login.dto.LoginRequest;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.infra.spring.security.JwtService;
import projeto.obuteco.model.repository.usuario.UsuariosRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {
    private final UsuariosRepository usuariosRepository;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public ResponseStatusException autenticaoDoLogin(LoginRequest loginRequest) {
        log.info("Autenticando usuário: {}", loginRequest.usuario());

        if(loginRequest == null || loginRequest.usuario() == null || loginRequest.senha() == null){
            return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos");
        }

        var userPassword = new UsernamePasswordAuthenticationToken(loginRequest.usuario(), loginRequest.senha());
        var authentication = authenticationManager.authenticate(userPassword);

        var usuario = (UsuariosEntity) authentication.getPrincipal();
        jwtService.gerarToken(usuario);

        //procura se existe o usuarios vindo do request
        /*
        usuariosRepository.findByUsuarioAndDeletedAtIsNull(loginRequest.usuario())
                .orElseThrow(() -> {
                    log.warn("Tentativa de login com usuário inexistente: {}", loginRequest.usuario());

                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos");});

        if (!usuario.getSenha().equals(loginRequest.senha())) {
            log.warn("Tentativa de login com senha inválida. Usuário: {}", loginRequest.usuario());

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos");
        }

         */

        log.info("Autenticação realizada com sucesso. Usuário: {}, ID: {}",
                usuario.getUsuario(),
                usuario.getId());

        return new ResponseStatusException(HttpStatus.OK, "Usuario Autenticado");
        /*
        UsuariosEntity usuario = usuariosRepository.findByUsuarioAndDeletedAtIsNull(loginRequest.usuario())
                .orElseThrow(() -> {
                    log.warn("Tentativa de login com usuário inexistente: {}", loginRequest.usuario());

                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos");});

        //verifica a senha se bate
        if (!usuario.getSenha().equals(loginRequest.senha())) {
            log.warn("Tentativa de login com senha inválida. Usuário: {}", loginRequest.usuario());

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos");
        }

        log.info("Autenticação realizada com sucesso. Usuário: {}, ID: {}",
                usuario.getUsuario(),
                usuario.getId());

        return usuario;

         */
    }

}
