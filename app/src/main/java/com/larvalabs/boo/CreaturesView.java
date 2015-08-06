package com.larvalabs.boo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreaturesView extends View {

    private static final int NUM_CREATURES = 10;

    private List<Creature> creatures = null;
    private PhysicsSystem system = null;

    private boolean faceVisible = true;

    private boolean introMode = false;

    public CreaturesView(Context context) {
        super(context);
        init(context, null);
    }

    public CreaturesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CreaturesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
    }

    public void setIntroMode(boolean introMode) {
        this.introMode = introMode;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (creatures == null) {
            creatures = new ArrayList<>();
            if (introMode) {
                float creatureSize = getWidth() / 9f;
                float creatureSizeSmall = creatureSize * 0.9f;
                float creatureSizeLarge = creatureSize * 1.1f;
                float yAmount = creatureSize / 15;
                float[] sizes = {creatureSize, creatureSizeSmall, creatureSize, creatureSizeLarge};
                float[] yOffsets = {0, yAmount, 0, yAmount / 2};
                float[] attackAngles = {3 * MathUtils.PI / 4, 3 * MathUtils.PI_OVER_2, MathUtils.PI, 0};
                system = new PhysicsSystem(getWidth(), sizes);
                system.setRepulsionFactor(0.25f);
                float x = -creatureSize - creatureSizeSmall * 2;
                x += creatureSize / 3f; // To account for the skinny exclamation point
                for (int i = 0; i < 4; i++) {
                    system.setSpringOffset(i, x, yOffsets[i]);
                    x += sizes[i] * 2;
                }
                CreatureInteraction creatureInteraction = new CreatureInteraction(creatures);
                for (int i = 0; i < 4; i++) {
                    Creature creature;
                    if (i == 0) {
                        creature = new LetterCreature(getContext(), sizes[i], system, i, creatureInteraction, R.drawable.letter_b, 0.35f);
                    } else if (i == 3) {
                        creature = new LetterCreature(getContext(), sizes[i], system, i, creatureInteraction, R.drawable.exclamation_point, -0.8f);
                    } else {
                        creature = new Creature(getContext(), sizes[i], system, i, creatureInteraction);
                    }
                    creature.reorient(attackAngles[i]);
                    creatures.add(creature);
                }
                Creature b = creatures.remove(0);
                creatures.add(2, b);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setFaceVisible(true);
                        ((BooActivity)getContext()).endIntro();
                    }
                }, 5000);
            } else {
                float[] sizes = new float[NUM_CREATURES];
                for (int i = 0; i < NUM_CREATURES; i++) {
                    sizes[i] = MathUtils.random(getWidth() / 20, getWidth() / 8);
                }
                system = new PhysicsSystem(getWidth(), sizes);
                CreatureInteraction creatureInteraction = new CreatureInteraction(creatures);
                for (int i = 0; i < NUM_CREATURES; i++) {
//                Creature creature = new LetterCreature(getContext(), sizes[i], system, i, creatureInteraction, R.drawable.letter_b);
                    Creature creature = new Creature(getContext(), sizes[i], system, i, creatureInteraction);
                    creature.reorient();
                    creatures.add(creature);
                }
            }
            setFaceVisible(false);
        }
        system.update();
        for (Creature creature : creatures) {
            creature.draw(canvas, getWidth(), getHeight(), !introMode);
        }
        invalidate();
    }

    public boolean setFaceVisible(boolean faceVisible) {
        if (faceVisible != this.faceVisible) {
            long[] delays = new long[creatures.size()];
            if (introMode) {
                delays[0] = 0;
                delays[1] = 0;
                delays[2] = 2000;
                delays[3] = 2400;
            } else {
                delays[0] = 0;
                for (int i = 1; i < delays.length; i++) {
                    if (MathUtils.flip(0.333f)) {
                        delays[i] = delays[i - 1] + MathUtils.random(250, 1000);
                    } else {
                        delays[i] = delays[i - 1] + MathUtils.random(3000, 8000);
                    }
                }
            }
            if (!faceVisible && !introMode) {
                // When returning, re-order the creatures randomly
                Collections.shuffle(creatures);
            }
            for (int i = 0; i < creatures.size(); i++) {
                Creature creature = creatures.get(i);
                if (faceVisible) {
                    creature.hide();
                } else {
                    if (!introMode) {
                        creature.reorient();
                    }
                    creature.comeBack(delays[i]);
                }
            }
            this.faceVisible = faceVisible;
            return true;
        } else {
            return false;
        }
    }

}
