package fr.campus.grog.SG.controller;

import fr.campus.grog.SG.dto.GameCreationParams;
import fr.campus.grog.SG.dto.MoveParams;
import fr.campus.grog.SG.service.GameService;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/games")
    public Collection<Game> getGames(@RequestHeader("X-UserId") UUID userId) {
        return this.gameService.getUserGame(userId);
    }

    @PostMapping("/games")
    public Game createGame(@RequestHeader("X-UserId") UUID userId, @RequestBody GameCreationParams requestParams) {
        return this.gameService.createGame(userId, requestParams);
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
    public Game move(@RequestHeader("X-UserId") UUID userId, @PathVariable UUID gameId, @RequestBody MoveParams moveParams) {
        return this.gameService.move(userId, gameId, moveParams);
    }

}
