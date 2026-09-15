package fr.campus.grog.SG;

import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Stream;

@Repository
public class JdbcGameDao implements GameDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final Map<String, GamePlugin> plugins = new HashMap<>();

    // Spring automatically collects and injects(new()) all beans implementing GamePlugin
    public JdbcGameDao(NamedParameterJdbcTemplate jdbcTemplate, List<GamePlugin> pluginList) {
        this.jdbcTemplate = jdbcTemplate;
        for(GamePlugin plugin : pluginList){
            this.plugins.put(plugin.getId(), plugin);
        }
    }

    // For findAll() and findById(UUID gameId) methods :
    // NOTE: In this CURRENT step (raw JDBC), we deliberately reconstruct a simplified game instance.
    // Full state restoration (exact original UUID, persistent token board positions,
    // and player mapping) requires complex relational joins or entity mapping,
    // which illustrates the Object-Relational Impedance Mismatch and will be properly
    // resolved using Spring Data JPA in NEXT step. (in the future GameDao Implementation Class)

    @Override
    public Stream<Game> findAll() {
        String sql = "SELECT * FROM games";

        List<Game> games = this.jdbcTemplate.query(sql, Map.of(), (rs, rowNum) -> {
            // a. Read columns from PostgreSQL row 'rs':
            String factoryId = rs.getString("factory_id");
            int boardSize = rs.getInt("board_size");
            // b. Retrieve the corresponding plugin from our map:
            GamePlugin plugin = this.plugins.get(factoryId);
            // c. Recreate the game:
            return plugin.createGame(null, boardSize);
        });

        return games.stream();
    }

    @Override
    public Optional<Game> findById(UUID gameId) {
        String sql = "SELECT * FROM games WHERE id = :gameUUID";
        Map<String, Object> params = Map.of("gameUUID", gameId.toString());

        // 1. query() takes the SQL, the params, and a RowMapper lambda (rs, rowNum) -> ...
        List<Game> games = this.jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            // a. Read columns from PostgreSQL row 'rs':
            String factoryId = rs.getString("factory_id");
            int boardSize = rs.getInt("board_size");
            // b. Retrieve the corresponding plugin from our map:
            GamePlugin plugin = this.plugins.get(factoryId);
            // c. Recreate the game:
            return plugin.createGame(null, boardSize);
        });

        // 2. Convert the resulting List<Game> into an Optional<Game>
        return games.stream().findFirst();
    }

    @Override
    public Game upsert(Game game) {
        // 1. The SQL query template with named placeholders (:id, :factory_id, etc.)
        String sql = """
        INSERT INTO games (id, factory_id, board_size, player_ids, status, current_player_id)
        VALUES (:gameUUID, :factory_id, :board_size, :player_ids, :status, :current_player_id)
        ON CONFLICT (id) DO UPDATE SET
            status = EXCLUDED.status,
            current_player_id = EXCLUDED.current_player_id
        """;

        // 2. The parameter dictionary: links each SQL placeholder to the Java value
        Map<String, Object> params = new HashMap<>();
        params.put("gameUUID", game.getId().toString());
        params.put("factory_id", game.getFactoryId());
        params.put("board_size", game.getBoardSize());
        params.put("status", game.getStatus().name());
        params.put("player_ids", game.getPlayerIds().toString()); // converts [uuid1, uuid2] to String
        params.put("current_player_id", game.getCurrentPlayerId() != null ? game.getCurrentPlayerId().toString() : null);

        // 3. Execute the SQL order through Spring's template
        this.jdbcTemplate.update(sql, params);

        return game;
    }

    @Override
    public void delete(UUID gameId) {

        String sql = """
        DELETE FROM games WHERE id=:gameUUID
        """;

        Map<String, Object> params = new HashMap<>();
        params.put("gameUUID", gameId.toString());

        this.jdbcTemplate.update(sql, params);
    }

}
