package com.larvalabs.boo;

import java.util.List;

public class CreatureInteraction {

    private static final long NEW_ARRIVAL_DURATION = 1500;
    private static final float NEW_ARRIVAL_LOOK_CHANCE = 0.9f;

    private List<Creature> creatures;

    private Creature newCreature = null;
    private long arrivalTime = 0;

    public CreatureInteraction(List<Creature> creatures) {
        this.creatures = creatures;
    }

    public void creatureArrived(Creature creature, long time) {
        newCreature = creature;
        arrivalTime = time;
        notice(creature, time);
    }

    public void notice(Creature creature, long time) {
        for (Creature other : creatures) {
            if (other == creature) {
                break;
            } else {
                other.lookIfAble(time, creature);
            }
        }
    }

    public boolean isNewArrival(long time) {
        if (newCreature != null) {
            if (time - arrivalTime > NEW_ARRIVAL_DURATION) {
                newCreature = null;
            }
        }
        return newCreature != null;
    }

    public Creature getLookTarget(Creature creature) {
        if (newCreature != null && newCreature != creature && MathUtils.flip(NEW_ARRIVAL_LOOK_CHANCE)) {
            return newCreature;
        } else {
            Creature target;
            do {
                target = MathUtils.chooseAtRandom(creatures);
            } while (target == creature);
            return target;
        }
    }

}
