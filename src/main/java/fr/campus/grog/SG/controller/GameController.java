package fr.campus.grog.SG.controller;

import fr.campus.grog.SG.dto.GameCreationParams;
import fr.campus.grog.SG.dto.MoveParams;
import fr.campus.grog.SG.service.GameService;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@Tag(name = "Game Sessions", description = "Game lifecycle management (creation, retrieval, move execution)")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(summary = "List player games", description = "Returns all active or completed games in which the identified player participates.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Games list retrieved successfully"),
    })
    @GetMapping("/games")
    public Collection<Game> getGames(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UUID userId) {
        return this.gameService.getUserGame(userId);
    }

    @Operation(summary = "Create a new game", description = "Initializes a game board according to the game type (tictactoe, connect4, taquin) and associates the creator and optional opponents.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game created and persisted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters or unknown game type"),
            @ApiResponse(responseCode = "403", description = "Creator or opponent not recognized by Square Users service")
    })
    @PostMapping("/games")
    public Game createGame(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UUID userId,
            @RequestBody GameCreationParams requestParams) {
        return this.gameService.createGame(userId, requestParams);
    }

    @Operation(summary = "Get game by ID", description = "Returns the full game state, associated players, board size, and remaining tokens.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game found"),
            @ApiResponse(responseCode = "404", description = "No game found for this identifier")
    })
    @GetMapping("/games/{gameId}")
    public Game getGame(
            @Parameter(description = "UUID identifier of the game", required = true)
            @PathVariable UUID gameId) {
        return this.gameService.getGame(gameId);
    }

    @Operation(summary = "List available moves", description = "Computes and returns the collection of immediately playable cell positions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of permitted coordinates"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @GetMapping("/games/{gameId}/moves")
    public Collection<CellPosition> getMoves(
            @Parameter(description = "UUID identifier of the game", required = true)
            @PathVariable UUID gameId) {
        return this.gameService.getAvailableMoves(gameId);
    }

    @Operation(summary = "Play a move", description = "Moves a token to the target cell after verifying player identity and turn.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Move played successfully and game state updated"),
            @ApiResponse(responseCode = "400", description = "Invalid move or no movable token towards the target cell"),
            @ApiResponse(responseCode = "403", description = "Unauthorized player or not player's turn to play"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @PostMapping("/games/{gameId}/moves")
    public Game move(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "UUID identifier of the game", required = true)
            @PathVariable UUID gameId,
            @RequestBody MoveParams moveParams) {
        return this.gameService.move(userId, gameId, moveParams);
    }

}
