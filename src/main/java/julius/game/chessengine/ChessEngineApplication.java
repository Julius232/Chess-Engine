package julius.game.chessengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@EnableAutoConfiguration
@SpringBootApplication
public class ChessEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChessEngineApplication.class, args);
	}

}
