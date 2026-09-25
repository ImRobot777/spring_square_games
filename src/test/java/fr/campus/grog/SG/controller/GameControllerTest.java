package fr.campus.grog.SG.controller;

import fr.campus.grog.SG.dto.GameCreationParams;
import fr.campus.grog.SG.service.GameService;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock
    private GameService gameService;

    private MockMvc mockMvc;

    // Real Game instance so Jackson can serialize getters without crashing on Mockito proxy reflection internals
    private final Game sampleGame = new TicTacToeGameFactory().createGame(2, 3);

    @BeforeEach
    void setUp() {
        // Standalone setup: tests HTTP mapping and controller logic in isolation without booting full Spring context
        GameController controller = new GameController(gameService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()) // <-- Register the argument resolver
                .build();
    }

    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext(); // <-- Prevent ThreadLocal leakage
    }

    @Test
    void testCreateGame_ReturnsHttp200() throws Exception {

        UUID userId = UUID.randomUUID();

        // Seed the SecurityContext with the authenticated user ID for AuthenticationPrincipal resolution
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, List.of()));

        when(gameService.createGame(eq(userId), any(GameCreationParams.class))).thenReturn(sampleGame);

        String jsonPayload = """
        {
            "gameFactoryId": "tictactoe",
            "nbPlayers": 2,
            "boardSize": 3
        }
        """;

        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());

        verify(gameService).createGame(eq(userId), any(GameCreationParams.class));
    }

    @Test
    void testGetGames_ReturnsHttp200() throws Exception {
        UUID userId = UUID.randomUUID();
        // Seed the SecurityContext with the authenticated user ID for AuthenticationPrincipal resolution
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
        when(gameService.getUserGame(userId)).thenReturn(List.of(sampleGame));

        mockMvc.perform(get("/games"))
                .andExpect(status().isOk());

        verify(gameService).getUserGame(userId);
    }

    @Test
    void testGetGame_WhenExists_ReturnsHttp200() throws Exception {
        UUID gameId = sampleGame.getId();
        when(gameService.getGame(gameId)).thenReturn(sampleGame);

        mockMvc.perform(get("/games/{gameId}", gameId))
                .andExpect(status().isOk());

        verify(gameService).getGame(gameId);
    }

    @Test
    void testGetGame_WhenNotFound_ReturnsHttp404() throws Exception {
        UUID gameId = UUID.randomUUID();
        when(gameService.getGame(gameId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        mockMvc.perform(get("/games/{gameId}", gameId))
                .andExpect(status().isNotFound());

        verify(gameService).getGame(gameId);
    }

    @Test
    void testGetAvailableMoves_ReturnsHttp200() throws Exception {
        UUID gameId = UUID.randomUUID();
        when(gameService.getAvailableMoves(gameId)).thenReturn(List.of(new CellPosition(0, 0)));

        mockMvc.perform(get("/games/{gameId}/moves", gameId))
                .andExpect(status().isOk());

        verify(gameService).getAvailableMoves(gameId);
    }

    @Test
    void testMove_ReturnsHttp200() throws Exception {
        UUID gameId = sampleGame.getId();
        UUID userId = UUID.randomUUID();
        // Seed the SecurityContext with the authenticated user ID for AuthenticationPrincipal resolution
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
        when(gameService.move(eq(userId), eq(gameId), any())).thenReturn(sampleGame);

        String jsonPayload = """
        {
            "target": {"x": 1, "y": 1},
            "source": null
        }
        """;

        mockMvc.perform(post("/games/{gameId}/moves", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());

        verify(gameService).move(eq(userId), eq(gameId), any());
    }


}
