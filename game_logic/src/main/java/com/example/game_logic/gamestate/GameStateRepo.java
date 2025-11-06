package com.example.game_logic.gamestate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameStateRepo extends JpaRepository<GameState, Long> {
}
