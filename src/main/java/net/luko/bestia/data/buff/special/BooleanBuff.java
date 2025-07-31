package net.luko.bestia.data.buff.special;

public class BooleanBuff extends SpecialBuff<Boolean>{

    public BooleanBuff(String id){
        super(id, false, 1);
    }

    @Override
    public Boolean computeValue(int level) {
        return level > 0;
    }
}