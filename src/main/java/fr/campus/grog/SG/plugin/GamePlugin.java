package fr.campus.grog.SG.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.TokenPosition;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface GamePlugin {

    String getId();
    String getName(Locale locale);
    Game createGame(Integer playerCount, Integer boardSize);

    // Reconstitutes an existing game with its exact ID, players, and token positions from DB
    Game reloadGame(UUID id,
                    int boardSize,
                    List<UUID> playerIds,
                    Collection<TokenPosition<UUID>> boardTokens,
                    Collection<TokenPosition<UUID>> removedTokens);
}
