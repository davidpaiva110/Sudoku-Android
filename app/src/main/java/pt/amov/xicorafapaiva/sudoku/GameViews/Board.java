package pt.amov.xicorafapaiva.sudoku.GameViews;

import android.content.Context;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;

import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;



public class Board extends View {


    public static final int BOARD_SIZE = 9;

    // Tabuleiro
    //private int [][] board = null;
    private int [][] board = {
            {0, 1, 0, 2, 0, 3, 0, 4, 0 },
            {5, 0, 6, 0, 7, 0, 8, 0, 9 },
            {0, 1, 0, 2, 0, 3, 0, 4, 0 },
            {5, 0, 6, 0, 7, 0, 8, 0, 9 },
            {0, 1, 0, 2, 0, 3, 0, 4, 0 },
            {5, 0, 6, 0, 7, 0, 8, 0, 9 },
            {0, 1, 0, 2, 0, 3, 0, 4, 0 },
            {5, 0, 6, 0, 7, 0, 8, 0, 9 },
            {0, 1, 0, 2, 0, 3, 0, 4, 0 },
    };

    // Paints
    private Paint paintMainLines;
    private Paint paintSubLines;
    private Paint paintMainNumbers;
    private Paint paintSmallNumbers;

    public Board(Context context) {
        super(context);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        createPaints();
        drawBoard(canvas);
    }


    void createPaints(){
        paintMainLines = new Paint(Paint.DITHER_FLAG);
        paintMainLines.setStyle(Paint.Style.FILL_AND_STROKE);
        paintMainLines.setColor(Color.BLACK);
        paintMainLines.setStrokeWidth(8);

        paintSubLines = new Paint(paintMainLines);
        paintSubLines.setStrokeWidth(3);

        paintMainNumbers = new Paint(paintSubLines);
        paintMainNumbers.setColor(Color.rgb(0,0,128));
        paintMainNumbers.setTextSize(32);  //Default value que dpeois será recalculado tendo em conta o tamanho ca célula
        paintMainNumbers.setTextAlign(Paint.Align.CENTER);

        paintSmallNumbers = new Paint(paintMainNumbers);
        paintSmallNumbers.setTextSize(12);
        paintSmallNumbers.setStrokeWidth(2);
        paintSmallNumbers.setColor(Color.rgb(0x40, 0x80, 0xa0));
    }


    private void drawBoard(Canvas canvas){
        int w = getWidth();
        int cellW = w / BOARD_SIZE;
        int h = getHeight();
        int cellH = h / BOARD_SIZE;

        // Desenhar as linhas do tabuleiro
        for(int r = 0; r <= BOARD_SIZE; r++){
            canvas.drawLine(0, cellH * r, w, cellH*r, r % 3 == 0 ? paintMainLines : paintSubLines);
            canvas.drawLine(cellW * r, 0, cellW * r, h, r % 3 == 0 ? paintMainLines : paintSubLines);
        }

        if(board == null)
            return;

        //Alterar o tamanho da letra em função do tamanho de cada célula
        paintMainNumbers.setTextSize(cellH/2);
        paintSmallNumbers.setTextSize(cellH/4);

        for(int r = 0; r < BOARD_SIZE; r++){
            for(int c = 0; c < BOARD_SIZE; c++){
                int n = board[r][c];
                if(n != 0){  // 0 representa espaço em branco
                    // Calcular o centro de cada célula
                    int x = c * cellW + cellW / 2;
                    int y = r * cellH + cellH /2 + cellH/6;    //cellH/6 -> deslocamento para centrar o número em altura
                    canvas.drawText(""+n, x, y, paintMainNumbers);
                } else {
                    //Primeira posição célula pequenina
                    int x = c *cellW + cellW / 6;
                    int y = r * cellH + cellH / 6;
                    List<Integer> possibilities = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 8);
                    Collections.shuffle(possibilities);
                    Random rnd = new Random(SystemClock.elapsedRealtime());
                    possibilities = possibilities.subList(0, rnd.nextInt(5)+1);
                    for(int p = 1; p <= BOARD_SIZE; p++){
                        if(possibilities.contains(p)){
                            int xp = x + (p -1) % 3 * cellW/3 ;
                            int yp = y + (p -1) / 3 * cellH/3 + cellH/9;
                            canvas.drawText(""+p, xp, yp, paintSmallNumbers);
                        }
                    }
                }
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){  // Importante para dizermos que manifestamos interesse nele
            return  true;
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            int px = (int) event.getX();
            int py = (int) event.getY();
            int w = getWidth();
            int cellW = w / BOARD_SIZE;
            int h = getHeight();
            int cellH = h / BOARD_SIZE;

            //Célula Selecionada
            int cellX = px / cellW;
            int cellY = py / cellH;

            board[cellY][cellX] = 8;
            invalidate(); // faz um refresh
        }
        return super.onTouchEvent(event);
    }

    public void setBoard(int [][] board){
        this.board = board;
    }

}
