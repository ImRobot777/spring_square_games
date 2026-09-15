package fr.campus.grog.SG.service;

import fr.campus.grog.SG.dao.GameDao;
import fr.campus.grog.SG.dto.GameCreationParams;
import fr.campus.grog.SG.dto.MoveParams;
import fr.campus.grog.SG.plugin.GamePlugin;

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

    private final Map<String, GamePlugin> plugins = new HashMap<>();
    private final GameDao gameDao;

    // Spring automatically collects and injects(new()) all beans implementing GamePlugin and GameDao
    public GameServiceImpl(List<GamePlugin> pluginList, GameDao gameDao) {
        this.gameDao = gameDao;
        for(GamePlugin plugin : pluginList){
            this.plugins.put(plugin.getId(), plugin);
        }
    }

    @Override
    public Game createGame(GameCreationParams requestParams) {
        // 1. Retrieve the plugin corresponding to the requested game type
        GamePlugin plugin = this.plugins.get(requestParams.gameFactoryId());

        // 2. Delegate game creation to the plugin
        Game game = plugin.createGame(requestParams.nbPlayers(), requestParams.boardSize());

        // 3. Save game instance for subsequent requests (e.g. GET /games/{id})
        return this.gameDao.upsert(game);
    }



    @Override
    public Game getGame(UUID gameId) {
        return this.gameDao.findById(gameId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
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
            this.gameDao.upsert(game);// For persistent Save in case that game is saved not in memory but in Data Base
        } catch (InvalidPositionException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return game;
    }
}
