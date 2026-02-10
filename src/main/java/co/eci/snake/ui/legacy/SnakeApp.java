package co.eci.snake.ui.legacy;

import co.eci.snake.concurrency.SnakeRunner;
import co.eci.snake.core.Board;
import co.eci.snake.core.Direction;
import co.eci.snake.core.GameController;
import co.eci.snake.core.Position;
import co.eci.snake.core.Snake;
import co.eci.snake.core.engine.GameClock;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public final class SnakeApp extends JFrame {

  private final Board board;
  private final GamePanel gamePanel;
  private final JButton startButton;
  private final JButton pauseButton;
  private final GameClock clock;
  private final GameController controller;
  private final java.util.List<Snake> snakes = new java.util.ArrayList<>();
  private final StatsPanel statsPanel;
  private final JDialog statsDialog;
  private ExecutorService executor;
  private boolean gameStarted = false;

  private static final Color[] PALETTE = {
      new Color(0, 170, 0),
      new Color(0, 160, 180),
      new Color(255, 100, 0),
      new Color(200, 50, 100),
      new Color(100, 100, 255),
      new Color(50, 200, 100),
      new Color(255, 200, 50),
      new Color(150, 75, 150),
      new Color(75, 150, 200),
      new Color(200, 150, 50),
      new Color(100, 200, 200),
      new Color(200, 100, 150),
      new Color(150, 200, 75),
      new Color(100, 100, 150),
      new Color(200, 200, 100),
      new Color(150, 100, 200),
      new Color(100, 150, 100),
      new Color(200, 100, 100),
      new Color(100, 200, 150),
      new Color(150, 150, 200)
  };

  public SnakeApp() {
    super("The Snake Race");
    this.board = new Board(35, 28);

    int N = Integer.getInteger("snakes", 2);
    for (int i = 0; i < N; i++) {
      int x = 2 + (i * 3) % board.width();
      int y = 2 + (i * 2) % board.height();
      var dir = Direction.values()[i % Direction.values().length];
      snakes.add(Snake.of(x, y, dir));
    }

    this.controller = new GameController(snakes);
    this.gamePanel = new GamePanel(board, () -> snakes);
    this.startButton = new JButton("Start");
    this.pauseButton = new JButton("Pause");
    pauseButton.setEnabled(false);

    this.statsPanel = new StatsPanel();
    this.statsDialog = new JDialog(this, "Game Statistics", false);
    statsDialog.add(statsPanel);
    statsDialog.setSize(450, 350);
    statsDialog.setLocationRelativeTo(this);

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(startButton);
    buttonPanel.add(pauseButton);

    setLayout(new BorderLayout());
    add(gamePanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pack();
    setLocationRelativeTo(null);

    this.clock = new GameClock(60, () -> SwingUtilities.invokeLater(gamePanel::repaint));

    startButton.addActionListener(e -> startGame());
    pauseButton.addActionListener(e -> togglePause());

    setupKeyBindings();

    setVisible(true);
  }

  private void startGame() {
    if (!gameStarted) {
      gameStarted = true;
      startButton.setEnabled(false);
      pauseButton.setEnabled(true);

      executor = Executors.newVirtualThreadPerTaskExecutor();
      snakes.forEach(s -> executor.submit(new SnakeRunner(s, board, controller)));

      controller.start();
      clock.start();
    }
  }

  private void togglePause() {
    if ("Pause".equals(pauseButton.getText())) {
      pauseButton.setText("Resume");
      controller.pause();
      clock.pause();
      showStats();
    } else {
      pauseButton.setText("Pause");
      controller.resume();
      clock.resume();
      statsDialog.setVisible(false);
    }
  }

  private void showStats() {
    var stats = snakes.stream()
        .map(s -> new co.eci.snake.core.SnakeStats(
            snakes.indexOf(s),
            s.snapshot().size(),
            0,
            true,
            controller.getElapsedTime()
        ))
        .toList();
    statsPanel.updateStats(stats);
    statsDialog.setVisible(true);
  }

  private void setupKeyBindings() {
    var player = snakes.get(0);
    InputMap im = gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap am = gamePanel.getActionMap();

    im.put(KeyStroke.getKeyStroke("LEFT"), "left");
    im.put(KeyStroke.getKeyStroke("RIGHT"), "right");
    im.put(KeyStroke.getKeyStroke("UP"), "up");
    im.put(KeyStroke.getKeyStroke("DOWN"), "down");
    im.put(KeyStroke.getKeyStroke("SPACE"), "pause");

    am.put("left", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        player.turn(Direction.LEFT);
      }
    });
    am.put("right", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        player.turn(Direction.RIGHT);
      }
    });
    am.put("up", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        player.turn(Direction.UP);
      }
    });
    am.put("down", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        player.turn(Direction.DOWN);
      }
    });
    am.put("pause", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (gameStarted && pauseButton.isEnabled()) {
          togglePause();
        }
      }
    });

    if (snakes.size() > 1) {
      var p2 = snakes.get(1);
      im.put(KeyStroke.getKeyStroke('A'), "p2-left");
      im.put(KeyStroke.getKeyStroke('D'), "p2-right");
      im.put(KeyStroke.getKeyStroke('W'), "p2-up");
      im.put(KeyStroke.getKeyStroke('S'), "p2-down");

      am.put("p2-left", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          p2.turn(Direction.LEFT);
        }
      });
      am.put("p2-right", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          p2.turn(Direction.RIGHT);
        }
      });
      am.put("p2-up", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          p2.turn(Direction.UP);
        }
      });
      am.put("p2-down", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          p2.turn(Direction.DOWN);
        }
      });
    }
  }


  public static final class GamePanel extends JPanel {
    private final Board board;
    private final Supplier snakesSupplier;
    private final int cell = 20;

    @FunctionalInterface
    public interface Supplier {
      List<Snake> get();
    }

    public GamePanel(Board board, Supplier snakesSupplier) {
      this.board = board;
      this.snakesSupplier = snakesSupplier;
      setPreferredSize(new Dimension(board.width() * cell + 1, board.height() * cell + 40));
      setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      var g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g2.setColor(new Color(220, 220, 220));
      for (int x = 0; x <= board.width(); x++)
        g2.drawLine(x * cell, 0, x * cell, board.height() * cell);
      for (int y = 0; y <= board.height(); y++)
        g2.drawLine(0, y * cell, board.width() * cell, y * cell);

      g2.setColor(new Color(255, 102, 0));
      for (var p : board.obstacles()) {
        int x = p.x() * cell, y = p.y() * cell;
        g2.fillRect(x + 2, y + 2, cell - 4, cell - 4);
        g2.setColor(Color.RED);
        g2.drawLine(x + 4, y + 4, x + cell - 6, y + 4);
        g2.drawLine(x + 4, y + 8, x + cell - 6, y + 8);
        g2.drawLine(x + 4, y + 12, x + cell - 6, y + 12);
        g2.setColor(new Color(255, 102, 0));
      }

      g2.setColor(Color.BLACK);
      for (var p : board.mice()) {
        int x = p.x() * cell, y = p.y() * cell;
        g2.fillOval(x + 4, y + 4, cell - 8, cell - 8);
        g2.setColor(Color.WHITE);
        g2.fillOval(x + 8, y + 8, cell - 16, cell - 16);
        g2.setColor(Color.BLACK);
      }

      Map<Position, Position> tp = board.teleports();
      g2.setColor(Color.RED);
      for (var entry : tp.entrySet()) {
        Position from = entry.getKey();
        int x = from.x() * cell, y = from.y() * cell;
        int[] xs = { x + 4, x + cell - 4, x + cell - 10, x + cell - 10, x + 4 };
        int[] ys = { y + cell / 2, y + cell / 2, y + 4, y + cell - 4, y + cell / 2 };
        g2.fillPolygon(xs, ys, xs.length);
      }

      g2.setColor(Color.BLACK);
      for (var p : board.turbo()) {
        int x = p.x() * cell, y = p.y() * cell;
        int[] xs = { x + 8, x + 12, x + 10, x + 14, x + 6, x + 10 };
        int[] ys = { y + 2, y + 2, y + 8, y + 8, y + 16, y + 10 };
        g2.fillPolygon(xs, ys, xs.length);
      }

      var snakes = snakesSupplier.get();
      int idx = 0;
      for (Snake s : snakes) {
        var body = s.snapshot().toArray(new Position[0]);
        Color snakeColor = idx < PALETTE.length ? PALETTE[idx] : generateColor(idx);

        for (int i = 0; i < body.length; i++) {
          var p = body[i];
          int shade = Math.max(0, 40 - i * 4);
          g2.setColor(new Color(
              Math.min(255, snakeColor.getRed() + shade),
              Math.min(255, snakeColor.getGreen() + shade),
              Math.min(255, snakeColor.getBlue() + shade)));
          g2.fillRect(p.x() * cell + 2, p.y() * cell + 2, cell - 4, cell - 4);
        }

        if (body.length > 0) {
          var head = body[0];
          int x = head.x() * cell + cell / 2;
          int y = head.y() * cell + cell / 2;
          g2.setColor(Color.WHITE);
          g2.setFont(new Font("Arial", Font.BOLD, 12));
          String num = String.valueOf(idx);
          FontMetrics fm = g2.getFontMetrics();
          int tx = x - fm.stringWidth(num) / 2;
          int ty = y + fm.getAscent() / 2;
          g2.drawString(num, tx, ty);
        }
        idx++;
      }
      g2.dispose();
    }

    private Color generateColor(int index) {
      float hue = (index % 20) / 20.0f;
      float saturation = 0.7f;
      float brightness = 0.9f;
      return Color.getHSBColor(hue, saturation, brightness);
    }
  }

  public static void launch() {
    SwingUtilities.invokeLater(SnakeApp::new);
  }
}
