package fr.campus.grog.SG;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;

import java.util.Collection;
import java.util.UUID;

public interface GameService {
    Game createGame(GameCreationParams requestParams);

    Game getGame(UUID id);

    Collection<CellPosition> getAvailableMoves(UUID id);

    Game move(UUID gameId, MoveParams moveParams);
}
