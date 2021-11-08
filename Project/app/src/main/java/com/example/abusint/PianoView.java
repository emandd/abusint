package com.example.abusint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import android.os.Handler;


public class PianoView extends View {
    public PianoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        black = new Paint();
        black.setColor(Color.BLACK);
        white = new Paint();
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL);
        yellow = new Paint();
        yellow.setColor(Color.LTGRAY);
        yellow.setStyle(Paint.Style.FILL);
        soundPlayer = new AudioSoundPlayer(context);

    }

    public static final int NB = 14;//количество клавиш
    private Paint black, yellow, white;
    private HashMap<Integer, Key> whites = new HashMap<>();
    private HashMap<Integer,Key> blacks = new HashMap<>();
    private int keyWidth, keyHeight;
    private AudioSoundPlayer soundPlayer;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        keyWidth = w/NB;
        keyHeight = h;
        int count = 15;
        for (int i=0; i<NB; i++){
            int left = i * keyWidth;
            int right = left+keyWidth;

            if (i==NB-1){
                right=w;
            }

            RectF rect = new RectF(left, 0, right, h);
            whites.put(i+1, new Key(rect, i+1));

            if (i!=0 && i!=3 && i!=7 && i!=10){
                rect = new RectF((float) (i - 1) * keyWidth + 0.5f * keyWidth + 0.25f * keyWidth, 0,
                        (float) i * keyWidth + 0.25f * keyWidth, 0.67f * keyHeight);
                blacks.put(count, new Key(rect, count));
                count++;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Key k:whites.values()){
            canvas.drawRect(k.rect, k.down ? yellow : white);
        }

        for (Key k:blacks.values()){
            canvas.drawRect(k.rect, k.down ? yellow : black);
        }

        for (int i=1; i<NB; i++){
            canvas.drawLine(i * keyWidth, 0, i * keyWidth, keyHeight, black);        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        boolean isDownAction = action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_MOVE;
        for (int touchIndex = 0; touchIndex < event.getPointerCount(); touchIndex++) {
            float x = event.getX(touchIndex);
            float y = event.getY(touchIndex);
            Key k = keyForCords(x,y);
            if (k!=null){
                k.down = isDownAction;
            }
        }
        ArrayList<Key> tmp = new ArrayList<>(whites.values());
        tmp.addAll(blacks.values());
        for (Key k:tmp){
            if (k.down){
                if (!soundPlayer.isNotePlaying(k.sound)){
                    soundPlayer.playNote(k.sound);
                    invalidate();
                }else{
                    releaseKey(k);
                }
            }else{
                soundPlayer.stopNote(k.sound);
                releaseKey(k);
            }
        }
        return true;
    }



    private Key keyForCords(float x, float y) {
        for (Key k: whites.values()){
            if (k.rect.contains(x,y)){
                return k;
            }
        }
    return null;
    }

    private void releaseKey(final Key k) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                k.down = false;
                handler.sendEmptyMessage(0);
            }
        }, 100);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            /* Code to handle the message */
        }
    };
}

