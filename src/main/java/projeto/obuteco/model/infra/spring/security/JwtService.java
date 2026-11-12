package projeto.obuteco.model.infra.spring.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {
    private final Algorithm algorithm;
    private final String issuer;

    public JwtService(
            @Value("${api.security.token.secret:token}") String secret,
            @Value("${api.security.token.issuer:o-buteco}") String issuer
    ){
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
    }

    public String gerarToken(UsuariosEntity usuario){
        try {
            return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(usuario.getUsuario())
                    .withClaim("id", usuario.getId())
                    .withClaim("cargo", usuario.getCargo().toString())
                    //.withClaim("role", usuario.getCargo().toString())
                    //.withClaim("role", usuario.getRole())
                    .withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .sign(algorithm);

        }catch (JWTCreationException e){
            throw new IllegalStateException("Erro ao gerar o token");
        }
    }

    public String validarToken(String token){
        try {
            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTCreationException e){
            throw new IllegalStateException("Token Invalido");
        }
    }
}
