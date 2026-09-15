package fr.campus.grog.SG.dao;

import fr.le_campus_numerique.square_games.engine.Game;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

//Only concerne ACTIVE(CREATED) GAME INSTANCES
public interface GameDao {
    Stream<Game> findAll();
    Optional<Game> findById(UUID gameId);
    Game upsert(Game game);
    void delete(UUID gameId);
}