package fr.campus.grog.SG.dao;

import fr.campus.grog.SG.plugin.TicTacToePlugin;
import fr.le_campus_numerique.square_games.engine.Game;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JpaGameDaoTest {

    @Autowired
    private GameDao gameDao;

    @Autowired
    private TicTacToePlugin ticTacToePlugin;

    @Test
    void testUpsertAndFindById() {
        // Create a new 3x3 TicTacToe game with 2 players
        Game newGame = ticTacToePlugin.createGame(2, 3);
        UUID gameId = newGame.getId();

        // Save game into PostgreSQL through JPA DAO
        gameDao.upsert(newGame);

        // Reload the game from PostgreSQL
        Optional<Game> retrievedGameOpt = gameDao.findById(gameId);

        // Assert game was properly persisted and reloaded
        assertTrue(retrievedGameOpt.isPresent(), "Game should be present in the database");
        Game retrievedGame = retrievedGameOpt.get();

        assertEquals(gameId, retrievedGame.getId());
        assertEquals(newGame.getFactoryId(), retrievedGame.getFactoryId());
        assertEquals(3, retrievedGame.getBoardSize());
        assertEquals(2, retrievedGame.getPlayerIds().size());
        assertEquals(newGame.getRemainingTokens().size(), retrievedGame.getRemainingTokens().size());
    }

    @Test
    void testDelete() {
        // Create and persist a game
        Game newGame = ticTacToePlugin.createGame(2, 3);
        UUID gameId = newGame.getId();
        gameDao.upsert(newGame);

        // Verify it is present before deletion
        assertTrue(gameDao.findById(gameId).isPresent());

        // Delete game from database
        gameDao.delete(gameId);

        // Verify it is no longer found
        Optional<Game> deletedGameOpt = gameDao.findById(gameId);
        assertTrue(deletedGameOpt.isEmpty(), "Game should no longer exist after deletion");
    }
}
