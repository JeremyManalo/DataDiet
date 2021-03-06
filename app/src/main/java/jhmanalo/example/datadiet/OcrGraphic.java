/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jhmanalo.example.datadiet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;

//import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.List;

import jhmanalo.example.datadiet.camera.GraphicOverlay;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class OcrGraphic extends GraphicOverlay.Graphic {

    private int id;

    private static final int TEXT_COLOR = Color.WHITE;

    private static Paint rectPaint;
    private static Paint textPaint;
    private final TextBlock text;
    private boolean red;

    OcrGraphic(GraphicOverlay overlay, TextBlock text, boolean red) {
        super(overlay);

        this.text = text;
        this.red = red;

        if (rectPaint == null) {
            rectPaint = new Paint();
            rectPaint.setColor(TEXT_COLOR);
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setStrokeWidth(4.0f);
        }

        if (textPaint == null) {
            textPaint = new Paint();
            textPaint.setColor(TEXT_COLOR);
            textPaint.setTextSize(54.0f);
        }

        if (this.red)
        {
            rectPaint = new Paint();
            rectPaint.setColor(Color.RED);
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setStrokeWidth(4.0f);
            textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(54.0f);
        }


        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TextBlock getTextBlock() {
        return text;
    }

    /**
     * Checks whether a point is within the bounding box of this graphic.
     * The provided point should be relative to this graphic's containing overlay.
     * @param x An x parameter in the relative context of the canvas.
     * @param y A y parameter in the relative context of the canvas.
     * @return True if the provided point is contained within this graphic's bounding box.
     */
    public boolean contains(float x, float y) {
        // TODO: Check if this graphic's text contains this point.
        if (text == null) {
            return false;
        }
        RectF rect = new RectF(text.getBoundingBox());
        rect = translateRect(rect);
        return rect.contains(x, y);
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        // TODO: Draw the text onto the canvas.
        if (text == null) {
            return;
        }

        // Draws the bounding box around the TextBlock.
        RectF rect = new RectF(text.getBoundingBox());
        rect = translateRect(rect);

        Log.d("OcrGraphic", "text.getValue is " + text.getValue());
        String[] arr = text.getValue().split(" ");

        if (OcrDetectorProcessor.makeRed)
        {
            rectPaint.setColor(Color.RED);
            textPaint.setColor(Color.RED);
            /*for (String s : arr)
            {
                String cleanS = s.trim();
                cleanS = cleanS.toLowerCase();

                Log.d("OcrGraphic", "s is " + cleanS);
                Log.d("OcrGraphic", "scanned is " + OcrDetectorProcessor.scanned);
                if (cleanS.equals(OcrDetectorProcessor.scanned))
                {
                    rectPaint.setColor(Color.RED);
                    textPaint.setColor(Color.RED);
                    Log.d("OcrGraphic", "paint it red");
                }
                else
                {
                    rectPaint.setColor(Color.WHITE);
                    textPaint.setColor(Color.WHITE);
                }
            }*/
        }
        else
        {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // yourMethod();
                    rectPaint.setColor(Color.WHITE);
                    textPaint.setColor(Color.WHITE);
                }
            }, 8000);

        }

        canvas.drawRect(rect, rectPaint);

        List<? extends Text> textComponents = text.getComponents();
        for(Text currentText : textComponents) {
            float left = translateX(currentText.getBoundingBox().left);
            float bottom = translateY(currentText.getBoundingBox().bottom);
            canvas.drawText(currentText.getValue(), left, bottom, textPaint);
        }

        // Render the text at the bottom of the box.
        //canvas.drawText(text.getValue(), rect.left, rect.bottom, textPaint);
    }
}
