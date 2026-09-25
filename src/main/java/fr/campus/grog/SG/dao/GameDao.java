package fr.campus.grog.SG.dao;

import fr.le_campus_numerique.square_games.engine.Game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//Only concerne ACTIVE(CREATED) GAME INSTANCES
public interface GameDao {
    List<Game> findAll();
    Optional<Game> findById(UUID gameId);
    Game upsert(Game game);
    void delete(UUID gameId);
}