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
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@Tag(name = "Parties de jeu", description = "Gestion du cycle de vie des parties (création, consultation, exécution des coups)")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(summary = "Lister les parties d'un joueur", description = "Retourne l'ensemble des parties actives ou terminées auxquelles le joueur identifié participe.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des parties récupérée avec succès"),
            @ApiResponse(responseCode = "400", description = "En-tête X-UserId manquant ou mal formé")
    })
    @GetMapping("/games")
    public Collection<Game> getGames(
            @Parameter(description = "Identifiant UUID du joueur demandeur", required = true)
            @RequestHeader("X-UserId") UUID userId) {
        return this.gameService.getUserGame(userId);
    }

    @Operation(summary = "Créer une nouvelle partie", description = "Initialise un plateau de jeu selon le type (tictactoe, connect4, taquin) et associe le créateur et d'éventuels adversaires.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partie créée et persistée avec succès"),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides ou type de jeu inconnu"),
            @ApiResponse(responseCode = "403", description = "Créateur ou adversaire non reconnu par le service Square Users")
    })
    @PostMapping("/games")
    public Game createGame(
            @Parameter(description = "Identifiant UUID du joueur créateur", required = true)
            @RequestHeader("X-UserId") UUID userId,
            @RequestBody GameCreationParams requestParams) {
        return this.gameService.createGame(userId, requestParams);
    }

    @Operation(summary = "Consulter une partie par son identifiant", description = "Retourne l'état complet du jeu, les joueurs associés, la taille du plateau et les pions restants.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partie trouvée"),
            @ApiResponse(responseCode = "404", description = "Aucune partie trouvée pour cet identifiant")
    })
    @GetMapping("/games/{gameId}")
    public Game getGame(
            @Parameter(description = "Identifiant UUID de la partie", required = true)
            @PathVariable UUID gameId) {
        return this.gameService.getGame(gameId);
    }

    @Operation(summary = "Lister les coups possibles", description = "Calcule et renvoie la collection des positions de cases immédiatement jouables.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des coordonnées autorisées"),
            @ApiResponse(responseCode = "404", description = "Partie introuvable")
    })
    @GetMapping("/games/{gameId}/moves")
    public Collection<CellPosition> getMoves(
            @Parameter(description = "Identifiant UUID de la partie", required = true)
            @PathVariable UUID gameId) {
        return this.gameService.getAvailableMoves(gameId);
    }

    @Operation(summary = "Jouer un coup", description = "Déplace un pion vers la case cible en vérifiant l'identité du joueur et son tour de jeu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coup joué avec succès et état du jeu actualisé"),
            @ApiResponse(responseCode = "400", description = "Coup invalide ou aucun pion déplaçable vers la case cible"),
            @ApiResponse(responseCode = "403", description = "Joueur non autorisé ou ce n'est pas son tour de jouer"),
            @ApiResponse(responseCode = "404", description = "Partie introuvable")
    })
    @PostMapping("/games/{gameId}/moves")
    public Game move(
            @Parameter(description = "Identifiant UUID du joueur qui tente de jouer", required = true)
            @RequestHeader("X-UserId") UUID userId,
            @Parameter(description = "Identifiant UUID de la partie", required = true)
            @PathVariable UUID gameId,
            @RequestBody MoveParams moveParams) {
        return this.gameService.move(userId, gameId, moveParams);
    }

}
