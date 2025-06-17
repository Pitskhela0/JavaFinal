package chess.model;

public class Clock {
    private int hh, mm, ss;

    public Clock(int hh, int mm, int ss) {
        this.hh = hh;
        this.mm = mm;
        this.ss = ss;
    }

    public boolean outOfTime() {
        return hh == 0 && mm == 0 && ss == 0;
    }

    public void decr() {
        if (mm == 0 && ss == 0) {
            hh--;
            mm = 59;
            ss = 59;
        } else if (ss == 0) {
            mm--;
            ss = 59;
        } else {
            ss--;
        }
    }

    public String getTime() {
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }
}
