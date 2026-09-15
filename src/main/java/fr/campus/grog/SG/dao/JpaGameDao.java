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
            if (token.removed) {
                continue; // Ignore removed tokens during active board restoration
            }

            UUID ownerId = token.ownerId != null ? UUID.fromString(token.ownerId) : null;
            boolean isOnBoard = (token.x != null && token.y != null);

            if (isOnBoard) {
                boardTokens.add(new TokenPosition<>(ownerId, token.name, token.x, token.y));
            } else {
                remainingTokens.add(new TokenPosition<>(ownerId, token.name, 0, 0));
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

        List<UUID> playerIds = new ArrayList<>();
        for (String element : cleaned.split(",")) {
            String trimmed = element.trim();
            if (!trimmed.isEmpty()) {
                playerIds.add(UUID.fromString(trimmed));
            }
        }
        return playerIds;
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
        GameEntity entity = new GameEntity();
        entity.id = game.getId().toString();
        entity.factoryId = game.getFactoryId();
        entity.boardSize = game.getBoardSize();
        entity.playerIds = game.getPlayerIds().toString();

        // 1. Tokens currently placed on the board
        for (Map.Entry<CellPosition, Token> entry : game.getBoard().entrySet()) {
            CellPosition pos = entry.getKey();
            Token token = entry.getValue();
            entity.tokens.add(createTokenEntity(token, pos.x(), pos.y(), false));
        }

        // 2. Tokens in reserve (not placed yet, coordinates are null)
        for (Token token : game.getRemainingTokens()) {
            entity.tokens.add(createTokenEntity(token, null, null, false));
        }

        // 3. Removed/captured tokens (coordinates are null, removed is true)
        for (Token token : game.getRemovedTokens()) {
            entity.tokens.add(createTokenEntity(token, null, null, true));
        }

        return entity;
    }

    // Helper to build a GameTokenEntity cleanly without duplicating mapping code
    private GameTokenEntity createTokenEntity(Token token, Integer x, Integer y, boolean removed) {
        GameTokenEntity tokenEntity = new GameTokenEntity();
        tokenEntity.name = token.getName();
        tokenEntity.ownerId = token.getOwnerId().map(UUID::toString).orElse(null);
        tokenEntity.x = x;
        tokenEntity.y = y;
        tokenEntity.removed = removed;
        return tokenEntity;
    }
}