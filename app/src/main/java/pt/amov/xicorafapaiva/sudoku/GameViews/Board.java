package pt.amov.xicorafapaiva.sudoku.GameViews;

import android.content.Context;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.Paint;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import pt.amov.xicorafapaiva.sudoku.GameClasss.GameData;


public class Board extends View {

    public static final int BOARD_SIZE = 9;

    //Dados do jogo
    private GameData gameData;
    private int selectedValue = 0;
    private boolean onNotas = false;
    private boolean onApagar = false;

    // Paints
    private Paint paintMainLines;
    private Paint paintSubLines;
    private Paint paintPreSetNumbers;
    private Paint paintMainNumbers;
    private Paint paintSmallNumbers;

    public Board(Context context, GameData gameData) {
        super(context);
        this.gameData = gameData;
    }

    public Board(Context context, GameData gameData, int selectedValue, boolean onNotas, boolean onApagar) {
        super(context);
        this.gameData = gameData;
        this.selectedValue = selectedValue;
        this.onNotas = onNotas;
        this.onApagar = onApagar;

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
        paintMainNumbers.setTextSize(32);  //Default value que depois será recalculado tendo em conta o tamanho ca célula
        paintMainNumbers.setTextAlign(Paint.Align.CENTER);

        paintSmallNumbers = new Paint(paintMainNumbers);
        paintSmallNumbers.setTextSize(12);
        paintSmallNumbers.setStrokeWidth(2);
        paintSmallNumbers.setColor(Color.rgb(0x40, 0x80, 0xa0));

        paintPreSetNumbers = new Paint(paintMainNumbers);
        paintPreSetNumbers.setColor(Color.BLACK);
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

        if(gameData.getBoard() == null)
            return;

        //Alterar o tamanho da letra em função do tamanho de cada célula
        paintMainNumbers.setTextSize(cellH/2);
        paintPreSetNumbers.setTextSize(cellH/2);
        paintSmallNumbers.setTextSize(cellH/4);

        for(int r = 0; r < BOARD_SIZE; r++){
            for(int c = 0; c < BOARD_SIZE; c++){
                int n = gameData.getValue(r,c);
                if(n != 0){  // 0 representa espaço em branco
                    // Calcular o centro de cada célula
                    int x = c * cellW + cellW / 2;
                    int y = r * cellH + cellH /2 + cellH/6;    //cellH/6 -> deslocamento para centrar o número em altura
                    if(gameData.isPreSet(r,c))
                        canvas.drawText(""+n, x, y, paintPreSetNumbers);
                    else
                        canvas.drawText(""+n, x, y, paintMainNumbers);
                } else {
                    //Primeira posição célula pequenina
                    int x = c *cellW + cellW / 6;
                    int y = r * cellH + cellH / 6;
                    int [] notes = gameData.getCellNotes(r,c);
                    for(int p = 0; p < BOARD_SIZE; p++){
                        if(notes[p]!=0){
                            int xp = x + p % 3 * cellW/3 ;
                            int yp = y + p / 3 * cellH/3 + cellH/9;
                            canvas.drawText("" + notes[p], xp, yp, paintSmallNumbers);
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

                if(!gameData.isPreSet(cellY, cellX)) {
                    if(!onApagar && !onNotas)
                        gameData.setValue(cellY, cellX, selectedValue);
                    else if(!onApagar && onNotas) {
                        if(gameData.getCellNote(cellY, cellX, selectedValue - 1) == 0) //Verifica se o valor já está nas notas
                            gameData.setCellNote(cellY, cellX, selectedValue - 1, selectedValue); //Se não estiver coloca
                        else
                            gameData.setCellNote(cellY, cellX, selectedValue - 1, 0); //Se já estiver, retira
                    }
                    else if(onApagar){
                        if(gameData.getValue(cellY, cellX)>0)
                            gameData.setValue(cellY, cellX, 0);
                        else
                            gameData.resetCellNotes(cellY, cellX); //Apaga todas as notas
                    }
                    invalidate(); // faz um refresh
                }
        }
        return super.onTouchEvent(event);
    }

    public void setSelectedValue(int selectedValue) {
        this.selectedValue = selectedValue;
    }

    public void setOnNotas(boolean onNotas) {
        this.onNotas = onNotas;
    }

    public void setOnApagar(boolean onApagar) {
        this.onApagar = onApagar;
    }

    public boolean isOnNotas() {
        return onNotas;
    }

    public boolean isOnApagar() {
        return onApagar;
    }

    public void setGameData(GameData gameData) {
        this.gameData = gameData;
    }

    public GameData getGameData() { return gameData; }
    public int getSelectedValue() { return selectedValue; }
    public boolean getOnNotas(){return onNotas;}
    public boolean getOnApagar(){return onApagar;}
}
