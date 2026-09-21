package fr.campus.grog.SG.service;

import fr.campus.grog.SG.client.UserValidationClient;
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

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameDao gameDao;

    @Mock
    private GamePlugin tictactoePlugin;

    @Mock
    private UserValidationClient userValidationClient;

    private GameServiceImpl gameService;

    @BeforeEach
    void setUp() {
        // Configure plugin id for service registry initialization
        when(tictactoePlugin.getId()).thenReturn("tictactoe");

        List<GamePlugin> plugins = List.of(tictactoePlugin);
        this.gameService = new GameServiceImpl(plugins, gameDao, userValidationClient);
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
    void testCreateGame_WhenCreatorValid_DelegatesToPluginAndPersists() {
        UUID creatorId = UUID.randomUUID();
        GameCreationParams params = new GameCreationParams("tictactoe", 2, 3);
        Game createdGame = mock(Game.class);

        when(userValidationClient.isUserValid(creatorId)).thenReturn(true);
        when(tictactoePlugin.createGame(Set.of(creatorId), 3)).thenReturn(createdGame);
        when(gameDao.upsert(createdGame)).thenReturn(createdGame);

        Game result = gameService.createGame(creatorId, params);

        assertNotNull(result);
        assertEquals(createdGame, result);

        verify(userValidationClient, times(1)).isUserValid(creatorId);
        verify(tictactoePlugin, times(1)).createGame(Set.of(creatorId), 3);
        verify(gameDao, times(1)).upsert(createdGame);
    }

    @Test
    void testCreateGame_WhenCreatorInvalid_ThrowsForbidden() {
        UUID creatorId = UUID.randomUUID();
        GameCreationParams params = new GameCreationParams("tictactoe", 2, 3);

        when(userValidationClient.isUserValid(creatorId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gameService.createGame(creatorId, params)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Invalid creator user ID", exception.getReason());
        verify(gameDao, never()).upsert(any());
    }

    @Test
    void testCreateGame_WhenOpponentInvalid_ThrowsForbidden() {
        UUID creatorId = UUID.randomUUID();
        UUID invalidOpponentId = UUID.randomUUID();
        GameCreationParams params = new GameCreationParams("tictactoe", 2, 3, List.of(invalidOpponentId));

        when(userValidationClient.isUserValid(creatorId)).thenReturn(true);
        when(userValidationClient.isUserValid(invalidOpponentId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gameService.createGame(creatorId, params)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid opponent user ID"));
        verify(gameDao, never()).upsert(any());
    }

    @Test
    void testMove_WhenUserInvalid_ThrowsForbidden() {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        MoveParams moveParams = new MoveParams(new CellPosition(1, 1), null);

        when(userValidationClient.isUserValid(userId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gameService.move(userId, gameId, moveParams)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Invalid user ID", exception.getReason());
    }

    @Test
    void testMove_WhenNotCurrentPlayerTurn_ThrowsForbidden() {
        UUID userId = UUID.randomUUID();
        UUID otherPlayerId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        Game mockGame = mock(Game.class);

        when(userValidationClient.isUserValid(userId)).thenReturn(true);
        when(gameDao.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(mockGame.getCurrentPlayerId()).thenReturn(otherPlayerId);

        MoveParams moveParams = new MoveParams(new CellPosition(1, 1), null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gameService.move(userId, gameId, moveParams)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("It is not your turn", exception.getReason());
    }

    @Test
    void testMove_WhenNoMovableTokenMatchesTarget_ThrowsBadRequest() {
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Game mockGame = mock(Game.class);

        when(userValidationClient.isUserValid(userId)).thenReturn(true);
        when(gameDao.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(mockGame.getCurrentPlayerId()).thenReturn(userId);

        // Simulate a game where remaining tokens have empty allowed moves
        Token token = mock(Token.class);
        when(token.canMove()).thenReturn(true);
        when(token.getAllowedMoves()).thenReturn(Set.of()); // No moves allowed for target
        when(mockGame.getRemainingTokens()).thenReturn(List.of(token));

        // TicTacToe move: target is specified (1st param), source is null (2nd param)
        MoveParams moveParams = new MoveParams(new CellPosition(1, 1), null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gameService.move(userId, gameId, moveParams)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("No movable token found", exception.getReason());
    }

    @Test
    void testGetUserGame_FiltersGamesContainingUserId() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Game gameWithUser = mock(Game.class);
        when(gameWithUser.getPlayerIds()).thenReturn(Set.of(userId, otherUserId));

        Game gameWithoutUser = mock(Game.class);
        when(gameWithoutUser.getPlayerIds()).thenReturn(Set.of(otherUserId));

        when(gameDao.findAll()).thenReturn(Stream.of(gameWithUser, gameWithoutUser));

        Collection<Game> userGames = gameService.getUserGame(userId);

        assertEquals(1, userGames.size());
        assertTrue(userGames.contains(gameWithUser));
        assertFalse(userGames.contains(gameWithoutUser));
    }
}
