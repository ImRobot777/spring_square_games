package fr.campus.grog.SG;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
import fr.le_campus_numerique.square_games.engine.connectfour.ConnectFourGameFactory;
import fr.le_campus_numerique.square_games.engine.taquin.TaquinGameFactory;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameServiceImpl implements GameService {

    private final Map<String, GameFactory> factories = Map.of(
            "tictactoe", new TicTacToeGameFactory(),
            "15 puzzle", new TaquinGameFactory(),
            "connect4", new ConnectFourGameFactory()
    );

    private final Map<UUID, Game> gameStorage = new ConcurrentHashMap<>();

    @Override
    public Game createGame(GameCreationParams requestParams) {
        GameFactory gameFactory = this.factories.get(requestParams.gameFactoryId());
        Game game = gameFactory.createGame(requestParams.nbPlayers(), requestParams.boardSize());
        this.gameStorage.put(game.getId(), game); // Sauvegarde pour les futures requêtes GET
        return game;
    }

    @Override
    public Game getGame(UUID gameId) {
        return this.gameStorage.get(gameId);
    }


    @Override
    public Collection<CellPosition> getAvailableMoves(UUID gameId) {
        Game game = this.getGame(gameId);
        if (game == null) {
            return List.of();
        }

        Set<CellPosition> moves = new HashSet<>();
        for (Token token : game.getRemainingTokens()) {
            moves.addAll(token.getAllowedMoves());
        }

        for (Token token : game.getBoard().values()) {
            moves.addAll(token.getAllowedMoves());
        }

        return moves;
    }


    @Override
    public Game move(UUID gameId, MoveParams moveParams) {
        Game game = this.getGame(gameId);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        Token tokenToMove = null;

        if (moveParams.source() != null) {
            // Cas du Taquin : le pion est sur le plateau à la case de départ
            tokenToMove = game.getBoard().get(moveParams.source());
        } else {
            // Cas du Morpion : on cherche dans la réserve le pion actif qui peut aller sur target
            for (Token token : game.getRemainingTokens()) {
                if (token.canMove() && token.getAllowedMoves().contains(moveParams.target())) {
                    tokenToMove = token;
                    break;
                }
            }
        }

        if (tokenToMove == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No movable token found");
        }

        try {
            tokenToMove.moveTo(moveParams.target());
        } catch (InvalidPositionException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return game;
    }
}
