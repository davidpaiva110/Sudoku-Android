package pt.amov.xicorafapaiva.sudoku.GameViews;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.Paint;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


import pt.amov.xicorafapaiva.sudoku.GameClasss.GameData;
import pt.amov.xicorafapaiva.sudoku.GameClasss.GameHistoryData;
import pt.amov.xicorafapaiva.sudoku.GameClasss.GameHistoryViewModel;
import pt.amov.xicorafapaiva.sudoku.R;


public class Board extends View {

    public static final int BOARD_SIZE = 9;
    public static final int INVALID_TIME = 2000;

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
    private Paint paintPlayer2MainNumbers;
    private Paint paintSmallNumbers;
    private Paint paintPlayer2SmallNumbers;
    private Paint paintWrongNumbers;
    private Paint paintSmallWrongNumbers;

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
        paintSmallNumbers.setColor(Color.rgb(0,0,128));

        paintPreSetNumbers = new Paint(paintMainNumbers);
        paintPreSetNumbers.setColor(Color.BLACK);

        paintWrongNumbers = new Paint(paintMainNumbers);
        paintWrongNumbers.setColor(Color.RED);

        paintSmallWrongNumbers = new Paint(paintSmallNumbers);
        paintSmallWrongNumbers.setColor(Color.RED);

        paintPlayer2SmallNumbers = new Paint(paintSmallNumbers);
        paintPlayer2SmallNumbers.setColor(Color.rgb(11, 102, 35));

        paintPlayer2MainNumbers = new Paint(paintMainNumbers);
        paintPlayer2MainNumbers.setColor(Color.rgb(11, 102, 35));
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
        paintPlayer2MainNumbers.setTextSize(cellH/2);
        paintWrongNumbers.setTextSize(cellH/2);
        paintPreSetNumbers.setTextSize(cellH/2);
        paintSmallNumbers.setTextSize(cellH/4);
        paintPlayer2SmallNumbers.setTextSize(cellH/4);
        paintSmallWrongNumbers.setTextSize(cellH/4);

        for(int r = 0; r < BOARD_SIZE; r++){
            for(int c = 0; c < BOARD_SIZE; c++){
                int n = gameData.getValue(r,c);
                // Calcular o centro de cada célula
                int x = c * cellW + cellW / 2;
                int y = r * cellH + cellH /2 + cellH/6;    //cellH/6 -> deslocamento para centrar o número em altura
                if(n != 0){  // 0 representa espaço em branco
                    if(gameData.isPreSet(r,c))
                        canvas.drawText(""+n, x, y, paintPreSetNumbers);
                    else {
                        if(gameData.getGameMode() == 0 || gameData.isFinished()){
                            canvas.drawText("" + n, x, y, paintMainNumbers);
                        }
                        else {
                            if (gameData.getPlayerOfInsertedNumber(r, c) == 1)
                                canvas.drawText("" + n, x, y, paintMainNumbers);
                            else if (gameData.getPlayerOfInsertedNumber(r, c) == 2)
                                canvas.drawText("" + n, x, y, paintPlayer2MainNumbers);
                        }
                    }
                } else if(!gameData.numberIsValid(r, c)) {
                    n = gameData.getInvalidNumber(r ,c);
                    canvas.drawText("" + n, x, y, paintWrongNumbers);
                    Thread th = new Thread(new RunnableInvalidNumber(r, c, n, false));
                    th.start();
                } else {
                    //Primeira posição célula pequenina
                    x = c *cellW + cellW / 6;
                    y = r * cellH + cellH / 6;
                    int [] notes = null;
                    if(gameData.getPlayer() == 1)
                        notes = gameData.getCellNotes(r,c);
                    else if(gameData.getPlayer() == 2)
                        notes = gameData.getPlayer2CellNotes(r,c);
                    for(int p = 0; p < BOARD_SIZE; p++){
                        int xp = x + p % 3 * cellW/3 ;
                        int yp = y + p / 3 * cellH/3 + cellH/9;
                        if(notes[p]!=0){
                            if(gameData.getPlayer() == 1)
                                canvas.drawText("" + notes[p], xp, yp, paintSmallNumbers);
                            else if(gameData.getPlayer() == 2)
                                canvas.drawText("" + notes[p], xp, yp, paintPlayer2SmallNumbers);
                        }
                        else if(!gameData.noteIsValid(r, c, p)) {
                            n = gameData.getInvalidNote(r ,c, p);
                            canvas.drawText("" + n, xp, yp, paintSmallWrongNumbers);
                            Thread th = new Thread(new RunnableInvalidNumber(r, c, n, true));
                            th.start();
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


            if(!gameData.isPreSet(cellY, cellX) && !gameData.isFinished()) {
                if(!onApagar && !onNotas && gameData.getValue(cellY, cellX) == 0) {
                    gameData.setValue(cellY, cellX, selectedValue);
                    gameData.validateNumber(cellY, cellX);
                    if(gameData.getValue(cellY, cellX) != 0){ //Se o número inserido for válido
                        gameData.validateNotesAfterNewValidNumber(cellY, cellX);
                        gameData.setPlayerOfInsertedNumber(cellY, cellX);
                        gameData.setCorrectNumberTime();
                        gameData.incrementPlayerScore();
                        gameData.checkTerminateGame();
                        if(gameData.isFinished()){
                            // =========== Gravar os resultados do jogo ===========
                            saveGameResult();
                            // ====================================================
                            AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle)
                                    .setTitle(R.string.strGanhou)
                                    .setMessage(R.string.strTerminouJogo)
                                    .setIcon(android.R.drawable.ic_dialog_info)
                                    .setPositiveButton(R.string.strOK, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Activity activity = (Activity)getContext();
                                            activity.finish();
                                        }
                                    }) //Ao clicar no botão voltar à página principal. Como fazer?
                                    .create();
                            Button btn;
                            btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            if(btn != null){
                                btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            }
                            dialog.show();
                        }
                    }
                } else if(!onApagar && onNotas) {
                    if(gameData.getPlayer() == 1) {
                        if (gameData.getCellNote(cellY, cellX, selectedValue - 1) == 0) { //Verifica se o valor já está nas notas
                            gameData.setCellNote(cellY, cellX, selectedValue - 1, selectedValue); //Se não estiver coloca
                            gameData.validateNumber(cellY, cellX, selectedValue, 1);
                        } else
                            gameData.setCellNote(cellY, cellX, selectedValue - 1, 0); //Se já estiver, retira
                    } else if(gameData.getPlayer() == 2){
                        if (gameData.getPlayer2CellNote(cellY, cellX, selectedValue - 1) == 0) { //Verifica se o valor já está nas notas
                            gameData.setPlayer2CellNote(cellY, cellX, selectedValue - 1, selectedValue); //Se não estiver coloca
                            gameData.validateNumber(cellY, cellX, selectedValue, 2);
                        } else
                            gameData.setPlayer2CellNote(cellY, cellX, selectedValue - 1, 0); //Se já estiver, retira
                    }
                }
                else if(onApagar){
                    if(gameData.getValue(cellY, cellX)>0 && gameData.getPlayerOfInsertedNumber(cellY, cellX) == gameData.getPlayer()) {
                        gameData.setValue(cellY, cellX, 0);
                        gameData.decrementPlayerScore();
                    }
                    else {
                        if(gameData.getPlayer() == 1)
                            gameData.resetCellNotes(cellY, cellX); //Apaga todas as notas do jogador 1
                        else if(gameData.getPlayer() == 2)
                            gameData.resetPlayer2CellNotes(cellY, cellX); //Apaga todas as notas do jogador 2
                    }
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

    class RunnableInvalidNumber implements Runnable{

        private int row;
        private int column;
        private int value;
        private boolean isNota;

        public RunnableInvalidNumber(int row, int column, int value, boolean isNota) {
            this.row = row;
            this.column = column;
            this.value = value;
            this.isNota = isNota;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(INVALID_TIME);
                if(isNota){
                    if (gameData.getInvalidNote(row, column, value - 1) == value) {
                        gameData.resetInvalidNote(row, column, value - 1);
                        postInvalidate();
                    }
                }else {
                    if (gameData.getInvalidNumber(row, column) == value) {
                        gameData.resetInvalidNumber(row, column);
                        postInvalidate();
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Gravar os resultados do jogo
     */
    public void saveGameResult(){
        GameHistoryData ghd = new GameHistoryData( PlayerProfileActivity.getPlayerName(getContext()), "M1", gameData.getGameTime(), gameData.getPlayerScore(1));
        GameHistoryViewModel ghvm = new GameHistoryViewModel(getContext());
        ghvm.addNewGame(ghd);
        ghvm.saveHistory();
    }


}