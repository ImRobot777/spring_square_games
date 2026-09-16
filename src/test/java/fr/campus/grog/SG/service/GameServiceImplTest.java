package fr.campus.grog.SG.service;

import fr.campus.grog.SG.dao.GameDao;
import fr.campus.grog.SG.dto.GameCreationParams;
import fr.campus.grog.SG.dto.MoveParams;
import fr.campus.grog.SG.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameDao gameDao;

    @Mock
    private GamePlugin tictactoePlugin;

    private GameServiceImpl gameService;

    @BeforeEach
    void setUp() {
        // Configure plugin id for service registry initialization
        when(tictactoePlugin.getId()).thenReturn("tictactoe");

        List<GamePlugin> plugins = List.of(tictactoePlugin);
        this.gameService = new GameServiceImpl(plugins, gameDao);
    }

    @Test
    void testGetGame_WhenGameExists_ReturnsGame() {
        UUID gameId = UUID.randomUUID();
        Game mockGame = mock(Game.class);
        when(gameDao.findById(gameId)).thenReturn(Optional.of(mockGame));

        Game result = gameService.getGame(gameId);

        assertNotNull(result);
        assertEquals(mockGame, result);
        verify(gameDao, times(1)).findById(gameId);
    }

    @Test
    void testGetGame_WhenGameDoesNotExist_ThrowsNotFoundException() {
        UUID gameId = UUID.randomUUID();
        when(gameDao.findById(gameId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gameService.getGame(gameId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Game not found", exception.getReason());
    }

    @Test
    void testCreateGame_DelegatesToPluginAndPersists() {
        GameCreationParams params = new GameCreationParams("tictactoe", 2, 3);
        Game createdGame = mock(Game.class);

        when(tictactoePlugin.createGame(2, 3)).thenReturn(createdGame);
        when(gameDao.upsert(createdGame)).thenReturn(createdGame);

        Game result = gameService.createGame(params);

        assertNotNull(result);
        assertEquals(createdGame, result);

        // Verify the plugin was called with specified parameters
        verify(tictactoePlugin, times(1)).createGame(2, 3);
        // Verify the created game was saved into the DAO
        verify(gameDao, times(1)).upsert(createdGame);
    }

    @Test
    void testMove_WhenNoMovableTokenMatchesTarget_ThrowsBadRequest() {
        UUID gameId = UUID.randomUUID();
        Game mockGame = mock(Game.class);

        // Simulate a game where remaining tokens have empty allowed moves
        Token token = mock(Token.class);
        when(token.canMove()).thenReturn(true);
        when(token.getAllowedMoves()).thenReturn(Set.of()); // No moves allowed for target
        when(mockGame.getRemainingTokens()).thenReturn(List.of(token));

        when(gameDao.findById(gameId)).thenReturn(Optional.of(mockGame));

        // TicTacToe move: target is specified (1st param), source is null (2nd param)
        MoveParams moveParams = new MoveParams(new CellPosition(1, 1), null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gameService.move(gameId, moveParams)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("No movable token found", exception.getReason());
    }
}
