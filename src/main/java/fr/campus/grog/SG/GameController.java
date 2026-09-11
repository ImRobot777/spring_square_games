package fr.campus.grog.SG;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/games")
    public Game createGame(@RequestBody GameCreationParams requestParams) {
        return this.gameService.createGame(requestParams);
    }

    @GetMapping("/games/{gameId}")
    public Game getGame(@PathVariable UUID gameId) {
        return this.gameService.getGame(gameId);
    }

    @GetMapping("/games/{gameId}/moves")
    public Collection<CellPosition> getMoves(@PathVariable UUID gameId) {
        return this.gameService.getAvailableMoves(gameId);
    }

    @PostMapping("/games/{gameId}/moves")
    public Game move(@PathVariable UUID gameId, @RequestBody MoveParams moveParams) {
        return this.gameService.move(gameId, moveParams);
    }



}
