package server;

public interface AuthService {
    /*
    * Метод получения никнейма по логину и паролю
    * @return null если учетка не найдена
    * @return nickname, если учетка найдена
    * */
    String getNicknameByLoginAndPassword (String login, String password);
}
