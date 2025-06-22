package chess.view;

import clientSide.clients.PlayerClient;
import shared.GameState;
import shared.ChessMove;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NetworkBoardPanel extends JPanel implements MouseListener, MouseMotionListener {
    private static final int TILE = 64;
    private static final int BOARD_SIZE = 8;

    private PlayerClient playerClient; // null for spectators
    private GameState currentGameState;
    private boolean moveInputEnabled = false;

    // UI state for drag and drop
    private boolean dragging = false;
    private boolean ghostActive = false;
    private int dragX, dragY;
    private int sourceRow = -1, sourceCol = -1;
    private BufferedImage draggingPieceImage;

    // Square panels for the board
    private SquarePanel[][] squarePanels;

    // Image cache for pieces
    private Map<String, BufferedImage> pieceImages;

    public NetworkBoardPanel(PlayerClient playerClient) {
        this.playerClient = playerClient;
        this.currentGameState = new GameState(); // Initialize with empty board

        initializeBoard();
        loadPieceImages();

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // Constructor for spectators or when PlayerClient is set later
    public NetworkBoardPanel() {
        this(null);
    }

    // Method to set the player client after construction
    public void setPlayerClient(PlayerClient playerClient) {
        this.playerClient = playerClient;
    }

    private void initializeBoard() {
        setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        setPreferredSize(new Dimension(BOARD_SIZE * TILE, BOARD_SIZE * TILE));

        squarePanels = new SquarePanel[BOARD_SIZE][BOARD_SIZE];

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                SquarePanel panel = new SquarePanel(row, col);
                squarePanels[row][col] = panel;
                add(panel);
            }
        }
    }

    private void loadPieceImages() {
        pieceImages = new HashMap<>();
        String[] pieces = {"wpawn", "wrook", "wknight", "wbishop", "wqueen", "wking",
                "bpawn", "brook", "bknight", "bbishop", "bqueen", "bking"};

        for (String piece : pieces) {
            try {
                URL imageUrl = getClass().getResource("/images/" + piece + ".png");
                if (imageUrl != null) {
                    BufferedImage image = ImageIO.read(imageUrl);
                    pieceImages.put(piece, image);
                }
            } catch (Exception e) {
                System.err.println("Failed to load image for " + piece + ": " + e.getMessage());
            }
        }
    }

    public void updateFromGameState(GameState gameState) {
        this.currentGameState = gameState;

        // Update all square panels
        String[][] board = gameState.getBoard();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                squarePanels[row][col].setPiece(board[row][col]);
            }
        }

        // Enable/disable move input based on turn
        if (playerClient != null) {
            boolean isMyTurn = playerClient.isMyTurn(gameState);
            setMoveInputEnabled(isMyTurn && !gameState.isGameOver());
        }

        repaint();
    }

    public void setMoveInputEnabled(boolean enabled) {
        this.moveInputEnabled = enabled && (playerClient != null);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (ghostActive && draggingPieceImage != null) {
            g.drawImage(draggingPieceImage, dragX - TILE/2, dragY - TILE/2, TILE, TILE, null);
        }
    }

    private SquarePanel getSquarePanelAt(int x, int y) {
        Component comp = getComponentAt(x, y);
        return (comp instanceof SquarePanel) ? (SquarePanel) comp : null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!moveInputEnabled || currentGameState.isGameOver()) return;

        SquarePanel panel = getSquarePanelAt(e.getX(), e.getY());
        if (panel == null) return;

        String piece = panel.getPiece();
        if (piece == null) return;

        // Check if it's the correct player's piece
        boolean isWhitePiece = piece.startsWith("w");
        if (playerClient.isWhite() != isWhitePiece) return;

        // Start dragging
        sourceRow = panel.getRow();
        sourceCol = panel.getCol();
        draggingPieceImage = pieceImages.get(piece);

        if (draggingPieceImage != null) {
            dragging = true;
            ghostActive = true;
            dragX = e.getX();
            dragY = e.getY();
            panel.setDisplayPiece(false);
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!dragging) return;
        dragX = e.getX();
        dragY = e.getY();
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!dragging) return;

        SquarePanel targetPanel = getSquarePanelAt(e.getX(), e.getY());
        boolean moveAttempted = false;

        if (targetPanel != null) {
            int targetRow = targetPanel.getRow();
            int targetCol = targetPanel.getCol();

            // Check if it's a different square
            if (targetRow != sourceRow || targetCol != sourceCol) {
                // Create and send move
                ChessMove move = new ChessMove(sourceRow, sourceCol, targetRow, targetCol);
                playerClient.sendMoveToServer(move);
                moveAttempted = true;
            }
        }

        // restore the piece display when move was not valid
        if (sourceRow >= 0 && sourceCol >= 0) {
            squarePanels[sourceRow][sourceCol].setDisplayPiece(true);
        }

        // Shake window only for invalid drops (not same square)
        if (!moveAttempted && targetPanel == null) {
            shakeWindow();
        }

        // Reset dragging state
        dragging = false;
        ghostActive = false;
        sourceRow = -1;
        sourceCol = -1;
        draggingPieceImage = null;
        repaint();
    }

    private void shakeWindow() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window == null) return;

        Point originalLocation = window.getLocation();
        int shakeDistance = 10;
        int shakeTimes = 6;

        Timer shakeTimer = new Timer(50, null);
        shakeTimer.addActionListener(new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                int offset = (count % 2 == 0) ? shakeDistance : -shakeDistance;
                window.setLocation(originalLocation.x + offset, originalLocation.y);
                count++;
                if (count >= shakeTimes) {
                    shakeTimer.stop();
                    window.setLocation(originalLocation);
                }
            }
        });
        shakeTimer.start();
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void mouseMoved(MouseEvent e) { }

    // Inner class for individual square panels
    private class SquarePanel extends JPanel {
        private final int row;
        private final int col;
        private String piece;
        private boolean displayPiece = true;

        public SquarePanel(int row, int col) {
            this.row = row;
            this.col = col;
            setPreferredSize(new Dimension(TILE, TILE));
            setOpaque(true);
        }

        public int getRow() { return row; }
        public int getCol() { return col; }
        public String getPiece() { return piece; }

        public void setPiece(String piece) {
            this.piece = piece;
            repaint();
        }

        public void setDisplayPiece(boolean display) {
            this.displayPiece = display;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw square background
            Color backgroundColor = ((row + col) % 2 == 0)
                    ? new Color(240, 217, 181)  // Light squares
                    : new Color(181, 136, 99);  // Dark squares

            g.setColor(backgroundColor);
            g.fillRect(0, 0, getWidth(), getHeight());

            // Highlight if this is the current player's turn and they can move
            if (moveInputEnabled && piece != null && currentGameState != null) {
                boolean isWhitePiece = piece.startsWith("w");
                boolean isMyPiece = (playerClient != null && playerClient.isWhite() == isWhitePiece);
                boolean isMyTurn = (playerClient != null && playerClient.isMyTurn(currentGameState));

                if (isMyPiece && isMyTurn) {
                    g.setColor(new Color(255, 255, 0, 100)); // Yellow highlight
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }

            // Draw piece if present and should be displayed
            if (displayPiece && piece != null) {
                BufferedImage pieceImage = pieceImages.get(piece);
                if (pieceImage != null) {
                    g.drawImage(pieceImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        }
    }
}