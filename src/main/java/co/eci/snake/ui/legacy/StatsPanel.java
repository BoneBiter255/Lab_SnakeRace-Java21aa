package co.eci.snake.ui.legacy;

import co.eci.snake.core.SnakeStats;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class StatsPanel extends JPanel {
  private List<SnakeStats> stats;

  public StatsPanel() {
    setBackground(Color.WHITE);
    setPreferredSize(new Dimension(400, 300));
  }

  public void updateStats(List<SnakeStats> newStats) {
    this.stats = newStats;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (stats == null || stats.isEmpty()) {
      g.setColor(Color.BLACK);
      g.setFont(new Font("Arial", Font.PLAIN, 16));
      g.drawString("No snake statistics available", 20, 30);
      return;
    }

    g.setColor(Color.BLACK);
    g.setFont(new Font("Arial", Font.BOLD, 18));
    g.drawString("Game Statistics", 20, 30);

    SnakeStats longest = stats.stream()
        .filter(s -> s.alive())
        .max((a, b) -> Integer.compare(a.length(), b.length()))
        .orElse(null);

    g.setFont(new Font("Arial", Font.PLAIN, 14));
    int y = 70;

    if (longest != null) {
      g.setColor(Color.DARK_GRAY);
      g.drawString("Longest snake alive:", 20, y);
      y += 25;
      g.setColor(Color.BLUE);
      g.drawString("  Snake " + longest.id() + " - Length: " + longest.length(), 20, y);
      y += 40;
    }

    g.setColor(Color.DARK_GRAY);
    g.drawString("All snakes:", 20, y);
    y += 25;

    for (SnakeStats stat : stats) {
      String status = stat.alive() ? "Alive" : "Dead";
      String info = "  Snake " + stat.id() + ": Length " + stat.length() +
          " | Status: " + status + " | Time: " + (stat.survivalTime() / 1000) + "s";
      if (stat.alive()) {
        g.setColor(Color.GREEN);
      } else {
        g.setColor(Color.RED);
      }
      g.drawString(info, 20, y);
      y += 20;
    }
  }
}
