package messages.util;

public class UnitStatChange {
    public float HP;
    public int AP;
    public int MP;
    public int spice;
    public boolean isLoud;
    public boolean isSwallowed;

    public UnitStatChange(float HP, int AP, int MP, int spice, boolean isLoud, boolean isSwallowed) {
        this.HP = HP;
        this.AP = AP;
        this.MP = MP;
        this.spice = spice;
        this.isLoud = isLoud;
        this.isSwallowed = isSwallowed;
    }
}
