package projeto.OButeco_Back_End.controller.login.dto;
import projeto.OButeco_Back_End.model.Enum.CargoEnum;
import projeto.OButeco_Back_End.model.entity.usuario.UsuariosEntity;
public record LoginResponse(
        Long id,
        String nome,
        String usuario,
        CargoEnum cargo
) {


    public static LoginResponse de(UsuariosEntity usuariosEntity) {
        return new LoginResponse(
                usuariosEntity.getId(),
                usuariosEntity.getNome(),
                usuariosEntity.getUsuario(),
                usuariosEntity.getCargo()
        );
    }
}
