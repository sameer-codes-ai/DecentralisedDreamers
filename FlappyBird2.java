import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import java.io.*;

public class FlappyBird2 extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image advancedBackgroundImg;
    Image level3BackgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;
    Image spikeImg;

    // Bird class properties
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    public static void main(String[] args) {
        // Create the JFrame (the game window)
        JFrame frame = new JFrame("Flappy Bird");

        // Create an instance of the FlappyBird class
        FlappyBird2 game = new FlappyBird2();

        // Set the frame's properties
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null); // Center the window on the screen
        frame.setVisible(true);
    }

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe class properties
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }

        public void move() {
            x += velocityX;  // Move horizontally
        }
    }

    class MovingPipe extends Pipe {
        int direction = 1;

        MovingPipe(Image img) {
            super(img);
        }

        @Override
        public void move() {
            super.move();
            this.y += direction;
            if (this.y < -1500 || this.y > boardHeight - pipeHeight + 1500) {
                direction *= -0.5;
            }
        }
    }

    // New class for spiked pipes
    class Spike {
        int x, y, width, height;
        Image img;

        Spike(int x, int y, Image img) {
            this.x = x;
            this.y = y;
            this.width = 64;  // Width of the spike
            this.height = 20;  // Height of the spike
            this.img = img;
        }

        void draw(Graphics g) {
            g.drawImage(img, x, y, width, height, null);
        }
    }

    class SpikedPipe extends MovingPipe {
        boolean spikesOut = false;
        int spikeCycle = 0; // Track when spikes pop out
        ArrayList<Spike> spikes = new ArrayList<>();  // List to hold spikes

        SpikedPipe(Image img) {
            super(img);
        }

        @Override
        public void move() {
            super.move();
            spikeCycle++;
            if (spikeCycle >= 5) {
                spikesOut = !spikesOut;  // Toggle spikes every 5 frames
                spikeCycle = 0;
            }

            // Clear spikes if they were out last time and now they are in
            if (!spikesOut) {
                spikes.clear();
            } else {
                // Add spikes at the top and bottom of the pipe
                spikes.add(new Spike(x, y - 20, spikeImg));  // Top spike
                spikes.add(new Spike(x, y + height, spikeImg));  // Bottom spike
            }
        }

        public ArrayList<Spike> getSpikes() {
            return spikes;
        }
    }

    // Game logic
    Bird bird;
    int velocityX = -3;  // Reduced speed
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;
    double totalScore = 0;  // Variable to track total score
    int level = 1;
    float backgroundFade = 0.0f;

    // Constructor
    FlappyBird2() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon("flappybirdbg.png").getImage();
        advancedBackgroundImg = new ImageIcon("advancedflappybirdbg.jpg").getImage();
        level3BackgroundImg = new ImageIcon("level3flappybirdbg.png").getImage();
        birdImg = new ImageIcon("flappybird.png").getImage();
        topPipeImg = new ImageIcon("toppipe.png").getImage();
        bottomPipeImg = new ImageIcon("bottompipe.png").getImage();
        spikeImg = new ImageIcon("spike.png").getImage();  // Load spike image

        // Bird initialization
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // Timer to place pipes
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();

        // Game loop
        gameLoop = new Timer(1000 / 60, this);  // 60 FPS
        gameLoop.start();
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe;
        Pipe bottomPipe;

        if (level == 1) {
            // Static pipes
            topPipe = new Pipe(topPipeImg);
            bottomPipe = new Pipe(bottomPipeImg);
        } else if (level == 2) {
            // Moving pipes
            topPipe = new MovingPipe(topPipeImg);
            bottomPipe = new MovingPipe(bottomPipeImg);
        } else {
            // Spiked pipes
            topPipe = new SpikedPipe(topPipeImg);
            bottomPipe = new SpikedPipe(bottomPipeImg);
        }

        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Draw the basic background based on the level
        if (level == 1) {
            g2d.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);
        } else if (level == 2) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backgroundFade));
            g2d.drawImage(advancedBackgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else if (level == 3) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backgroundFade));
            g2d.drawImage(level3BackgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // Draw bird
        g2d.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        // Draw pipes and spikes
        for (Pipe pipe : pipes) {
            g2d.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);

            // Draw spikes if they are out
            if (pipe instanceof SpikedPipe) {
                SpikedPipe spikedPipe = (SpikedPipe) pipe;
                if (spikedPipe.spikesOut) {
                    for (Spike spike : spikedPipe.getSpikes()) {
                        spike.draw(g2d);  // Draw each spike
                    }
                }
            }
        }

        // Draw score and total score
        g2d.setColor(Color.white);
        g2d.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g2d.drawString("Game Over: " + (int) score, 10, 35);
        } else {
            g2d.drawString("Score: " + (int) score, 10, 35);
            g2d.drawString("Total Score: " + (int) totalScore, 10, 100);  // Display total score
        }
    }

    public void move() {
        // Bird movement
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);  // Limit bird's position

        // Pipes movement
        for (Pipe pipe : pipes) {
            pipe.move();

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                totalScore += 0.5;  // Update total score
                pipe.passed = true;
            }

            // Check collision with pipes
            if (collision(bird, pipe)) {
                gameOver = true;
            }

            // Check collision with spikes
            if (pipe instanceof SpikedPipe) {
                SpikedPipe spikedPipe = (SpikedPipe) pipe;
                for (Spike spike : spikedPipe.getSpikes()) {
                    if (collision(bird, spike)) {
                        gameOver = true;  // Game over if bird hits a spike
                    }
                }
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }

        // Level transitions
        if (score >= 10 && level == 1) {
            levelUp();
        } else if (score >= 20 && level == 2) {
            levelUpTo3();
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    boolean collision(Bird a, Spike b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    private void levelUp() {
        level = 2;
        pipes.clear();
        velocityX = -3;  // Adjusted speed for level 2
        backgroundFade = 0.6f;
        System.out.println("Level Up! Moving pipes introduced.");
    }

    private void levelUpTo3() {
        level = 3;
        pipes.clear();
        velocityX = -3;  // Adjusted speed for level 3
        backgroundFade = 0.9f;  // Fade to new background for level 3
        System.out.println("Level Up! Spiked pipes introduced.");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;

            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                try{
                FileWriter out= new FileWriter("C:/Users/samee/FlappyBird/score.txt",true);
                out.append(String.valueOf(totalScore)+"\n");
                out.close();}catch(Exception ex){System.out.println("File save failed");}
                //totalScore = 0;  // Reset total score
                level = 1;
                backgroundFade = 0.0f;
                gameLoop.start();
                placePipeTimer.start();
            }
        }
    }

    // Unused key events
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
