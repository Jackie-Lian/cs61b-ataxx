/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import ucb.gui2.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

import java.awt.event.MouseEvent;

import java.util.concurrent.ArrayBlockingQueue;

import static ataxx.PieceColor.*;
import static ataxx.Utils.*;

/** Widget for displaying an Ataxx board.
 *  @author Jackie Lian
 */
class BoardWidget extends Pad  {

    /** Length of side of one square, in pixels. */
    static final int SQDIM = 50;
    /** Number of squares on a side. */
    static final int SIDE = Board.SIDE;
    /** Radius of circle representing a piece. */
    static final int PIECE_RADIUS = 15;
    /** Dimension of a block. */
    static final int BLOCK_WIDTH = 40;

    /** Color of red pieces. */
    private static final Color RED_COLOR = Color.RED;
    /** Color of blue pieces. */
    private static final Color BLUE_COLOR = Color.BLUE;
    /** Color of painted lines. */
    private static final Color LINE_COLOR = Color.BLACK;
    /** Color of blank squares. */
    private static final Color BLANK_COLOR = Color.WHITE;
    /** Color of selected squared. */
    private static final Color SELECTED_COLOR = new Color(150, 150, 150);
    /** Color of blocks. */
    private static final Color BLOCK_COLOR = Color.BLACK;

    /** Stroke for lines. */
    private static final BasicStroke LINE_STROKE = new BasicStroke(1.0f);
    /** Stroke for blocks. */
    private static final BasicStroke BLOCK_STROKE = new BasicStroke(5.0f);

    /** A new widget sending commands resulting from mouse clicks
     *  to COMMANDQUEUE. */
    BoardWidget(ArrayBlockingQueue<String> commandQueue) {
        _commandQueue = commandQueue;
        setMouseHandler("click", this::handleClick);
        _dim = SQDIM * SIDE;
        _blockMode = false;
        setPreferredSize(_dim, _dim);
        setMinimumSize(_dim, _dim);
    }

    /** Indicate that SQ (of the form CR) is selected, or that none is
     *  selected if SQ is null. */
    void selectSquare(String sq) {
        if (sq == null) {
            _selectedCol = _selectedRow = 0;
        } else {
            _selectedCol = sq.charAt(0);
            _selectedRow = sq.charAt(1);
        }
        repaint();
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BLANK_COLOR);
        g.fillRect(0, 0, _dim, _dim);

        g.setColor(LINE_COLOR);
        g.setStroke(LINE_STROKE);
        for (int i = 0; i <= _dim; i += SQDIM) {
            g.drawLine(i, 0, i, _dim);
            g.drawLine(0, i, _dim, i);
        }
        if (_selectedCol != 0) {
            g.setColor(SELECTED_COLOR);
            g.fillRect((_selectedCol - 'a') * SQDIM,
                    ('7' - _selectedRow) * SQDIM, SQDIM, SQDIM);
        }

        for (char i = 'a'; i <= 'g'; i++) {
            for (char j = '1'; j <= '7'; j++) {

                if (_model.get(i, j).equals(BLOCKED)) {
                    drawBlock(g, (i - 'a') * SQDIM + SQDIM / 2,
                            ('7' - j) * SQDIM + SQDIM / 2);
                } else if (_model.get(i, j).equals(RED)) {
                    drawPiece(g, RED, (i - 'a') * SQDIM + SQDIM / 2,
                            ('7' - j) * SQDIM + SQDIM / 2);
                } else if (_model.get(i, j).equals(BLUE)) {
                    drawPiece(g, BLUE, (i - 'a') * SQDIM + SQDIM / 2,
                            ('7' - j) * SQDIM + SQDIM / 2);
                }
            }
        }

    }

    private void drawPiece(Graphics2D g, PieceColor color, int cx, int cy) {
        g.setStroke(LINE_STROKE);
        if (color.equals(RED)) {
            g.setColor(RED_COLOR);
        } else if (color.equals(BLUE)) {
            g.setColor(BLUE_COLOR);
        }
        g.fillOval(cx - PIECE_RADIUS, cy - PIECE_RADIUS,
                2 * PIECE_RADIUS, 2 * PIECE_RADIUS);
    }

    /** Draw a block centered at (CX, CY) on G. */
    void drawBlock(Graphics2D g, int cx, int cy) {
        g.setStroke(BLOCK_STROKE);
        g.setColor(BLOCK_COLOR);
        int ulX = cx - (BLOCK_WIDTH / 2);
        int ulY = cy + (BLOCK_WIDTH / 2);

        int mlX = cx - (BLOCK_WIDTH / 2);
        int mlY = cy;

        int llX = cx - (BLOCK_WIDTH / 2);
        int llY = cy - (BLOCK_WIDTH / 2);

        int uX = cx;
        int uY = cy + (BLOCK_WIDTH / 2);

        int lX = cx;
        int lY = cy - (BLOCK_WIDTH / 2);

        int urX = cx + (BLOCK_WIDTH / 2);
        int urY = cy + (BLOCK_WIDTH / 2);

        int rX = cx + (BLOCK_WIDTH / 2);
        int rY = cy;

        int lrX = cx + (BLOCK_WIDTH / 2);
        int lrY = cy - (BLOCK_WIDTH / 2);

        g.setColor(BLOCK_COLOR);
        g.setStroke(BLOCK_STROKE);
        g.drawLine(ulX, ulY, urX, urY);
        g.drawLine(urX, urY, lrX, lrY);
        g.drawLine(lrX, lrY, llX, llY);
        g.drawLine(llX, llY, ulX, ulY);
        g.drawLine(ulX, ulY, lrX, lrY);
        g.drawLine(llX, llY, urX, urY);
        g.drawLine(mlX, mlY, rX, rY);
        g.drawLine(uX, uY, lX, lY);


    }

    /** Clear selected block, if any, and turn off block mode. */
    void reset() {
        _selectedRow = _selectedCol = 0;
        setBlockMode(false);
    }

    /** Set block mode on iff ON. */
    void setBlockMode(boolean on) {
        _blockMode = on;
    }

    /** Issue move command indicated by mouse-click event WHERE. */
    private void handleClick(String unused, MouseEvent where) {
        int x = where.getX(), y = where.getY();
        char mouseCol, mouseRow;
        if (where.getButton() == MouseEvent.BUTTON1) {
            mouseCol = (char) (x / SQDIM + 'a');
            mouseRow = (char) ((SQDIM * SIDE - y) / SQDIM + '1');
            if (mouseCol >= 'a' && mouseCol <= 'g'
                && mouseRow >= '1' && mouseRow <= '7') {
                if (_blockMode) {
                    String place = "" + mouseCol + mouseRow;
                    _commandQueue.offer("block " + place);
                } else {
                    if (_selectedCol != 0) {
                        String start = "" + _selectedCol + _selectedRow;
                        String end = "" + mouseCol + mouseRow;
                        _commandQueue.offer(start + "-" + end);
                        _selectedCol = _selectedRow = 0;
                    } else {
                        _selectedCol = mouseCol;
                        _selectedRow = mouseRow;
                    }
                }
            }
        }
        repaint();
    }

    public synchronized void update(Board board) {
        _model = new Board(board);
        repaint();
    }

    /** Dimension of current drawing surface in pixels. */
    private int _dim;

    /** Model being displayed. */
    private static Board _model;

    /** Coordinates of currently selected square, or '\0' if no selection. */
    private char _selectedCol, _selectedRow;

    /** True iff in block mode. */
    private boolean _blockMode;

    /** Destination for commands derived from mouse clicks. */
    private ArrayBlockingQueue<String> _commandQueue;
}
