package fr.campus.grog.SG.dao;

import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Repository
public class InMemoryGameDao implements GameDao {

    private final Map<UUID, Game> gameStorage = new ConcurrentHashMap<>();

    /*
    @Override
    public Collection<Game> findAll() {
        return Collections.unmodifiableCollection(this.gameStorage.values());
    }
    */

    @Override
    public List<Game> findAll() {
        return gameStorage.values().stream().toList(); // .toList() <==> new way of .collect(Collectors.toList())

        // Alternative using ArrayList copy constructor (direct memory copy)
        //return new ArrayList<>(this.gameStorage.values());
        // Or using List.copyOf for an unmodifiable list
        //return List.copyOf(this.gameStorage.values());
    }

    @Override
    public Optional<Game> findById(UUID gameId) {
        return Optional.ofNullable(this.gameStorage.get(gameId));
    }

    @Override
    public Game upsert(Game game) {
        this.gameStorage.put(game.getId(), game);
        return game;
    }

    @Override
    public void delete(UUID gameId) {
        this.gameStorage.remove(gameId);
    }
}