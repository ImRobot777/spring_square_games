package fr.campus.grog.SG.dto;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import jakarta.validation.constraints.NotNull;

public record MoveParams(@NotNull CellPosition target, CellPosition source) {
}
