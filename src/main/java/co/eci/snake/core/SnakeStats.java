package co.eci.snake.core;

public record SnakeStats(
    int id,
    int length,
    int miceEaten,
    boolean alive,
    long survivalTime
) {
  public static SnakeStats of(int id, Snake snake, long currentTime, long startTime) {
    var snapshot = snake.snapshot();
    int length = snapshot.size();
    return new SnakeStats(id, length, 0, true, currentTime - startTime);
  }
}
