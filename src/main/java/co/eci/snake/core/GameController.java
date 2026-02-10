package co.eci.snake.core;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class GameController {
  private final AtomicReference<GameState> state = new AtomicReference<>(GameState.STOPPED);
  private long startTime;
  private long firstDeathTime = -1;
  private int firstDeadSnakeId = -1;

  public GameController(List<Snake> snakes) {
    this.startTime = System.currentTimeMillis();
  }

  public GameState getState() {
    return state.get();
  }

  public void start() {
    state.set(GameState.RUNNING);
    startTime = System.currentTimeMillis();
  }

  public void pause() {
    state.set(GameState.PAUSED);
  }

  public void resume() {
    state.set(GameState.RUNNING);
  }

  public void stop() {
    state.set(GameState.STOPPED);
  }

  public void checkAndWaitIfPaused() throws InterruptedException {
    while (state.get() == GameState.PAUSED) {
      Thread.sleep(50);
    }
  }

  public long getElapsedTime() {
    return System.currentTimeMillis() - startTime;
  }

  public void recordDeath(int snakeId) {
    if (firstDeadSnakeId == -1) {
      firstDeadSnakeId = snakeId;
      firstDeathTime = getElapsedTime();
    }
  }

  public int getFirstDeadSnakeId() {
    return firstDeadSnakeId;
  }

  public long getFirstDeathTime() {
    return firstDeathTime;
  }

  public boolean hasSnakeDied() {
    return firstDeadSnakeId != -1;
  }
}
