package pt.amov.xicorafapaiva.sudoku.GameClasss;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;

import pt.amov.xicorafapaiva.sudoku.R;


public class Board extends View implements View.OnTouchListener, Serializable {

    public static final int BOARD_HEIGHT = 9;
    public static final int BOARD_WIDTH = 9;
    public static final int HEIGHT_BOTTOM_FOR_BUTTON_AND_TIMER = 500;
    public static final int MARGE_BETWEEN_GRID_AND_BOTTOM = 50;

    // Dimensões do ecrã
    private int displayWidth;
    private int displayHeight;

    // Dimensões de uma célula
    private int cellWidth;
    private int cellHeight;

    // Tabuleiro
    private Cell [][] board;

    public Board(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnTouchListener(this);
        board = new Cell[BOARD_WIDTH][BOARD_HEIGHT];
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        //String msg = "x= " + x + " y=" + y;
        //Snackbar.make(this, msg, Snackbar.LENGTH_LONG).show();

        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setDefaultValues(canvas);
        drawBoard(canvas);
    }

    private void setDefaultValues(Canvas canvas){
        displayHeight = canvas.getHeight();
        displayWidth = canvas.getWidth();
        cellWidth = displayWidth/BOARD_WIDTH;
        cellHeight = (displayHeight- HEIGHT_BOTTOM_FOR_BUTTON_AND_TIMER)/BOARD_HEIGHT;
    }

    private void drawBoard(Canvas canvas){
        // Linhas Verticais
        for(int i=0; i<= BOARD_WIDTH; i++){
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            if(i % 3 == 0)
                paint.setStrokeWidth(8);
            else
                paint.setStrokeWidth(3);
            canvas.drawLine(cellWidth * i, 0, cellWidth * i, displayHeight - HEIGHT_BOTTOM_FOR_BUTTON_AND_TIMER, paint);
        }
        //Linhas Horizontais
        for(int i=0; i<= BOARD_HEIGHT; i++){
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            if(i % 3 == 0)
                paint.setStrokeWidth(8);
            else
                paint.setStrokeWidth(3);
            canvas.drawLine(0, cellHeight * i, displayWidth, cellHeight * i, paint);
        }
    }


}
