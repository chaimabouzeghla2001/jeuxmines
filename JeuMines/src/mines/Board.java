package mines;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.SecureRandom;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class Board extends JPanel {
	private static final long serialVersionUID = 6195235521361212179L;
	
	private static final int NUM_IMAGES = 13;
    private static final int CELL_SIZE = 15;

    private static final int COVER_FOR_CELL = 10;
    private static final int MARK_FOR_CELL = 10;
    private static final int EMPTY_CELL = 0;
    private static final int MINE_CELL = 9;
    private static final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL;
    private static final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;

    private static final int DRAW_MINE = 9;
    private static final int DRAW_COVER = 10;
    private static final int DRAW_MARK = 11;
    private static final int DRAW_WRONG_MARK = 12;

    private int[] field;
    private boolean inGame;
    private int minesLeft;
    private transient Image[] img;
    private int mines = 40;
    private int rows = 16;
    private int cols = 16;
    private int allCells;
    private JLabel statusbar;

    private SecureRandom random = new SecureRandom();
    public Board(JLabel statusbar) {

        this.statusbar = statusbar;

        img = new Image[NUM_IMAGES];

        for (int i = 0; i < NUM_IMAGES; i++) {
			img[i] =
                    (new ImageIcon(getClass().getClassLoader().getResource((i)
            			    + ".gif"))).getImage();
        }

        setDoubleBuffered(true);

        addMouseListener(new MinesAdapter());
        newGame();
    }


    public void newGame() {
        int currentCol;
        int mineIndex = 0;
        int position = 0;
        

        inGame = true;
        minesLeft = mines;

        allCells = rows * cols;
        field = new int[allCells];

        for (int i = 0; i < allCells; i++) {
            field[i] = COVER_FOR_CELL;
        }

        statusbar.setText(Integer.toString(minesLeft));

        while (mineIndex < mines) {
            position = (int) (allCells * random.nextDouble());

            if (position < allCells && field[position] != COVERED_MINE_CELL) {
                currentCol = position % cols;
                field[position] = COVERED_MINE_CELL;
                mineIndex++;

                if (currentCol > 0) {
                    incrementFieldCell(position - 1 - cols);
                    incrementFieldCell(position - 1);
                    incrementFieldCell(position + cols - 1);
                }

                incrementFieldCell(position - cols);
                incrementFieldCell(position + cols);

                if (currentCol < (cols - 1)) {
                    incrementFieldCell(position - cols + 1);
                    incrementFieldCell(position + cols + 1);
                    incrementFieldCell(position + 1);
                }
            }
        }
    }

    private void incrementFieldCell(int cell) {
        if (cell >= 0 && cell < allCells && field[cell] != COVERED_MINE_CELL) {
            field[cell] += 1;
        }
    }

    public void findEmptyCells(int j) {
        int currentCol = j % cols;
        int cell;

        // Check left column
        if (currentCol > 0) {
            cell = j - cols - 1;
            handleEmptyCell(cell);

            cell = j - 1;
            handleEmptyCell(cell);

            cell = j + cols - 1;
            handleEmptyCell(cell);
        }

        // Check top and bottom cells
        cell = j - cols;
        handleEmptyCell(cell);

        cell = j + cols;
        handleEmptyCell(cell);

        // Check right column
        if (currentCol < (cols - 1)) {
            cell = j - cols + 1;
            handleEmptyCell(cell);

            cell = j + cols + 1;
            handleEmptyCell(cell);

            cell = j + 1;
            handleEmptyCell(cell);
        }
    }

    private void handleEmptyCell(int cell) {
        if (cell >= 0 && field[cell] > MINE_CELL) {
            field[cell] -= COVER_FOR_CELL;
            if (field[cell] == EMPTY_CELL) {
                findEmptyCells(cell);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        int uncover = countUncoveredCells();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int cell = field[(i * cols) + j];

                if (inGame && cell == MINE_CELL) {
                    inGame = false;
                }

                if (!inGame) {
                    cell = getCellToDrawAfterGame(cell);
                } else {
                    cell = getCellToDrawDuringGame(cell);
                    if (cell == DRAW_COVER) {
                        uncover++;
                    }
                }

                g.drawImage(img[cell], (j * CELL_SIZE), (i * CELL_SIZE), this);
            }
        }

        updateGameStatus(uncover);
    }

    private int countUncoveredCells() {
        int count = 0;
        for (int cell : field) {
            if (cell > MINE_CELL && cell <= COVERED_MINE_CELL) {
                count++;
            }
        }
        return count;
    }

    private int getCellToDrawAfterGame(int cell) {
        if (cell == COVERED_MINE_CELL) {
            return DRAW_MINE;
        } else if (cell > COVERED_MINE_CELL) {
            return DRAW_WRONG_MARK;
        } else if (cell > MINE_CELL) {
            return DRAW_COVER;
        }
        return cell;
    }

    private int getCellToDrawDuringGame(int cell) {
        if (cell > COVERED_MINE_CELL) {
            return DRAW_MARK;
        } else if (cell > MINE_CELL) {
            return DRAW_COVER;
        }
        return cell;
    }

    private void updateGameStatus(int uncover) {
        if (uncover == 0 && inGame) {
            inGame = false;
            statusbar.setText("Game won");
        } else if (!inGame) {
            statusbar.setText("Game lost");
        }
    }

    class MinesAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            int cCol = x / CELL_SIZE;
            int cRow = y / CELL_SIZE;

            boolean shouldRepaint = false;

            if (!inGame) {
                newGame();
                repaint();
            }

            if (x < cols * CELL_SIZE && y < rows * CELL_SIZE) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    shouldRepaint = handleRightClick(cRow, cCol);
                } else {
                    shouldRepaint = handleLeftClick(cRow, cCol);
                }
            }

            if (shouldRepaint) {
                repaint();
            }
        }

        private boolean handleRightClick(int row, int col) {
            if (field[row * cols + col] > MINE_CELL) {
                boolean shouldRepaint = true;

                if (field[row * cols + col] <= COVERED_MINE_CELL) {
                    if (minesLeft > 0) {
                        field[row * cols + col] += MARK_FOR_CELL;
                        minesLeft--;
                        statusbar.setText(Integer.toString(minesLeft));
                    } else {
                        statusbar.setText("No marks left");
                    }
                } else {
                    field[row * cols + col] -= MARK_FOR_CELL;
                    minesLeft++;
                    statusbar.setText(Integer.toString(minesLeft));
                }

                return shouldRepaint;
            }

            return false;
        }

        private boolean handleLeftClick(int row, int col) {
            if (field[row * cols + col] > COVERED_MINE_CELL) {
                return false;
            }

            boolean shouldRepaint = true;

            if (field[row * cols + col] > MINE_CELL && field[row * cols + col] < MARKED_MINE_CELL) {
                field[row * cols + col] -= COVER_FOR_CELL;

                if (field[row * cols + col] == MINE_CELL) {
                    inGame = false;
                }

                if (field[row * cols + col] == EMPTY_CELL) {
                    findEmptyCells(row * cols + col);
                }
            }

            return shouldRepaint;
        }

    }
}
