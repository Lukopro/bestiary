package net.luko.bestia.util;

import net.luko.bestia.Bestia;
import net.luko.bestia.client.ClientConfigStore;
import net.luko.bestia.config.BestiaCommonConfig;

import java.util.ArrayList;
import java.util.List;

public class LevelFormula {
    private static String formula;

    public static void init(){
        formula = BestiaCommonConfig.KILLS_FORMULA.get();

        checkIfValid(BestiaCommonConfig.MONOTONY_CHECK.get());
    }

    public static void reinitializeForClient(){
        formula = ClientConfigStore.INSTANCE.killsFormula;
    }

    private static void checkIfValid(boolean fullCheck){
        if(!fullCheck) {
            List<Integer> samples = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                samples.add((int) (Math.random() * BestiaCommonConfig.MAX_LEVEL.get()));
            }
            samples = samples.stream().sorted().toList();

            for (int i = 1; i < samples.size(); i++) {
                if (getKills(samples.get(i - 1)) > getKills(samples.get(i))) {
                    String e = "Level to Kills mapping function is not strictly increasing: "
                            + getKills(samples.get(i - 1)) + " > " + getKills(samples.get(i)) + "for levels " + samples.get(i - 1) + ", " + samples.get(i);
                    Bestia.LOGGER.error(e);
                    throw new IllegalArgumentException(e);
                }
            }

            return;
        }

        for(int i = 1; i < BestiaCommonConfig.MAX_LEVEL.get(); i++){
            if (getKills(i - 1) > getKills(i)) {
                String e = "Level to Kills mapping function is not strictly increasing: "
                        + getKills(i - 1) + " > " + getKills(i) + "for levels " + (i - 1) + ", " + i;
                Bestia.LOGGER.error(e);
                throw new IllegalArgumentException(e);
            }
        }
    }

    // getKills() can be used for client or server
    public static int getKills(int level){
        return (int)Math.floor(FormulaParser.evaluate(formula, level));
    }

    // getLevel() is only built for server use at the moment
    public static int getLevel(int kills){
        int min = 0;
        int max = BestiaCommonConfig.MAX_LEVEL.get();

        if(kills < getKills(min)) return min;
        if(kills > getKills(max)) return max;

        while(min <= max){
            int mid = (min + max) / 2;
            int req = getKills(mid);

            if(req == kills){
                return mid;
            } else if(req < kills){
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }

        return max;
    }
}
