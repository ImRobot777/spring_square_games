package fr.campus.grog.SG.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InconsistentGameDefinitionException;
import fr.le_campus_numerique.square_games.engine.TokenPosition;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TicTacToePlugin implements GamePlugin {

    // Inject default values from application.properties
    @Value("${game.tictactoe.default-player-count}")
    private int defaultPlayerCount;

    @Value("${game.tictactoe.default-board-size}")
    private int defaultBoardSize;

    @Autowired
    private MessageSource messageSource;

    // Engine factory instance (encapsulation)
    private final TicTacToeGameFactory factory = new TicTacToeGameFactory();

    // 1. Return the game identifier
    @Override
    public String getId() {
        return this.factory.getGameFactoryId();
    }

    // 2. Return the game display name (temporary string before i18n step)
    @Override
    public String getName(Locale locale) {
        // Look up translated name based on current request locale
        return this.messageSource.getMessage("game.tictactoe.name", null, locale);
    }

    // 3. Create the game using fallback values if parameters are null
    @Override
    public Game createGame(Integer nbPlayers, Integer boardSize){
        int actualPlayers = nbPlayers!=null ? nbPlayers : this.defaultPlayerCount;
        int actualSize = boardSize != null ? boardSize : this.defaultBoardSize;
        return this.factory.createGame(actualPlayers, actualSize);
    }

    // 3bis. Create the game using valid player Ids
    @Override
    public Game createGame(Set<UUID> playerIds, Integer boardSize){
        int actualSize = boardSize != null ? boardSize : this.defaultBoardSize;
        return this.factory.createGame(actualSize, playerIds);
    }


    @Override
    public Game reloadGame(UUID id, int boardSize, List<UUID> playerIds,
                           Collection<TokenPosition<UUID>> boardTokens,
                           Collection<TokenPosition<UUID>> removedTokens) {
        try {
            return this.factory.createGameWithIds(id, boardSize, playerIds, boardTokens, removedTokens);
        } catch (InconsistentGameDefinitionException e) {
            throw new RuntimeException("Failed to reload game " + id, e);
        }
    }

}
