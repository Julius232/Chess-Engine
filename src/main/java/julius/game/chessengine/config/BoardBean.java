package julius.game.chessengine.config;

import julius.game.chessengine.board.Board;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.annotation.SessionScope;

public class BoardBean {

    @Bean
    @SessionScope
    public Board sessionScopedBoard() {
        return new Board();
    }

}
