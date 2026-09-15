package fr.campus.grog.SG.dao;

import fr.campus.grog.SG.entity.GameEntity;
import fr.campus.grog.SG.entity.GameEntityRepository;
import fr.campus.grog.SG.entity.GameTokenEntity;
import fr.campus.grog.SG.plugin.GamePlugin;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.Token;
import fr.le_campus_numerique.square_games.engine.TokenPosition;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Stream;

@Repository
@Primary
public class JpaGameDao implements GameDao {

    private final GameEntityRepository repository;
    private final Map<String, GamePlugin> plugins = new HashMap<>();

    // Spring automatically collects and injects all beans implementing GamePlugin
    public JpaGameDao(GameEntityRepository repository, List<GamePlugin> pluginList) {
        this.repository = repository;
        for (GamePlugin plugin : pluginList) {
            this.plugins.put(plugin.getId(), plugin);
        }
    }

    // Converts GameEntity from DB into a fully restored domain Game object
    private Game toGame(GameEntity entity) {
        GamePlugin plugin = this.plugins.get(entity.factoryId);
        UUID gameId = UUID.fromString(entity.id);
        List<UUID> playerIds = parsePlayerIds(entity.playerIds);
        List<TokenPosition<UUID>> boardTokens = new ArrayList<>();
        List<TokenPosition<UUID>> remainingTokens = new ArrayList<>();
        for (GameTokenEntity token : entity.tokens) {
            UUID ownerId = token.ownerId != null ? UUID.fromString(token.ownerId) : null;
            if (!token.removed) {
                if (token.x != null && token.y != null) {
                    boardTokens.add(new TokenPosition<>(ownerId, token.name, token.x, token.y));
                } else {
                    remainingTokens.add(new TokenPosition<>(ownerId, token.name, 0, 0));
                }
            }
        }
        return plugin.reloadGame(gameId, entity.boardSize, playerIds, remainingTokens, boardTokens);
    }

    // Helper to parse "[uuid1, uuid2]" String from DB back into List<UUID>
    private List<UUID> parsePlayerIds(String playerIdsStr) {
        if (playerIdsStr == null || playerIdsStr.isBlank()) {
            return List.of();
        }
        String cleaned = playerIdsStr.replace("[", "").replace("]", "").trim();
        if (cleaned.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(cleaned.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(UUID::fromString)
                .toList();
    }

    @Override
    public Stream<Game> findAll() {
        return this.repository.findAll().stream()
                .map(this::toGame);
    }

    @Override
    public Optional<Game> findById(UUID gameId) {
        return this.repository.findById(gameId.toString())
                .map(this::toGame);
    }

    @Override
    public Game upsert(Game game) {
        GameEntity gameEntity = toEntity(game);
        this.repository.save(gameEntity);
        return game;
    }

    @Override
    public void delete(UUID gameId) {
        this.repository.deleteById(gameId.toString());
    }

    // Converts domain Game object into relational GameEntity with all its tokens
    private GameEntity toEntity(Game game) {
        // 1. Create a new empty entity and map basic fields
        GameEntity entity = new GameEntity();
        entity.id = game.getId().toString();
        entity.factoryId = game.getFactoryId();
        entity.boardSize = game.getBoardSize();
        entity.playerIds = game.getPlayerIds().toString();

        // 2. Map tokens currently placed on the board
        for (Map.Entry<CellPosition, Token> entry : game.getBoard().entrySet()) {
            GameTokenEntity tokenEntity = new GameTokenEntity();
            tokenEntity.name = entry.getValue().getName();
            tokenEntity.ownerId = entry.getValue().getOwnerId().map(UUID::toString).orElse(null);
            tokenEntity.x = entry.getKey().x(); // CellPosition is a Java record: accessor is .x()
            tokenEntity.y = entry.getKey().y(); // Accessor is .y()
            tokenEntity.removed = false;
            entity.tokens.add(tokenEntity);
        }

        // 3. Map remaining tokens in reserve (not placed yet, coordinates are null)
        for (Token token : game.getRemainingTokens()) {
            GameTokenEntity tokenEntity = new GameTokenEntity();
            tokenEntity.name = token.getName();
            tokenEntity.ownerId = token.getOwnerId().map(UUID::toString).orElse(null);
            tokenEntity.x = null;
            tokenEntity.y = null;
            tokenEntity.removed = false;
            entity.tokens.add(tokenEntity);
        }

        // 4. Map removed/captured tokens (coordinates are null, removed is true)
        for (Token token : game.getRemovedTokens()) {
            GameTokenEntity tokenEntity = new GameTokenEntity();
            tokenEntity.name = token.getName();
            tokenEntity.ownerId = token.getOwnerId().map(UUID::toString).orElse(null);
            tokenEntity.x = null;
            tokenEntity.y = null;
            tokenEntity.removed = true;
            entity.tokens.add(tokenEntity);
        }

        // 5. Return a complete Game Entity Object READY to be saved in DB
        return entity;
    }
}