package fr.campus.grog.SG;

import fr.le_campus_numerique.square_games.engine.Game;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface GameDao {
    Stream<Game> findAll();
    Optional<Game> findById(UUID gameId);
    Game upsert(Game game);
    void delete(UUID gameId);
}