package fr.campus.grog.SG.service;

import fr.campus.grog.SG.dto.GameCreationParams;
import fr.campus.grog.SG.dto.MoveParams;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;

import java.util.Collection;
import java.util.UUID;

public interface GameService {

    Collection<Game> getUserGame(UUID userId);

    Game createGame(UUID userId, GameCreationParams requestParams);

    Game getGame(UUID id);

    Collection<CellPosition> getAvailableMoves(UUID id);

    Game move(UUID userId, UUID gameId, MoveParams moveParams);
}
