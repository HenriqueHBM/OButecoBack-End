package projeto.OButecoBack_End.Controller.Login.dto;

import projeto.OButecoBack_End.Entity.UsuariosEntity;

public record LoginResponse(
        Long id,
        String nome,
        String usuario,
        String cargo
) {
    public static LoginResponse de(UsuariosEntity usuariosEntity) {
        return new LoginResponse(
                usuariosEntity.getId(),
                usuariosEntity.getNome(),
                usuariosEntity.getUsuario(),
                usuariosEntity.getCargosEntity().getCargo()
        );
    }
}
