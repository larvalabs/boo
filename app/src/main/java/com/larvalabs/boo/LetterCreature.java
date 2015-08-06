package com.larvalabs.boo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

public class LetterCreature extends Creature {

    private final Bitmap bitmap;
    private Rect source = new Rect(0, 0, 1000, 1000);
    private RectF dest = new RectF();
    private float xOffset;

    public LetterCreature(Context context, float bodySize, PhysicsSystem system, int index, CreatureInteraction creatureInteraction, int svgResource, float xOffset) {
        super(context, bodySize, system, index, creatureInteraction);
        this.xOffset = xOffset;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeResource(context.getResources(), svgResource, options);
    }

    @Override
    public void doDraw(Canvas canvas, long t) {
        // Just draw the SVG letter
        paint.setColor(bodyColor);
        canvas.save();
        {
            float x = xOffset * bodySize;
            dest.set(-bodySize + x, -bodySize, bodySize + x, bodySize);
            canvas.drawBitmap(bitmap, source, dest, paint);
        }
        canvas.restore();
    }

}
