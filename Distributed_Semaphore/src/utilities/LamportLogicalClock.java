package utilities;

/**
 * A basic implementation of Lamport's logical clock for synchronizing
 * distributed systems.
 * @author Franklin D. Worrell
 * @version 20 November 2017
 */
public class LamportLogicalClock {

    private int time;

    /**
     * Creates a new logical clock and sets its time to zero.
     */
    public LamportLogicalClock() {
        this.time = 0;
    }

    /**
     * Returns the time on this logical clock.
     * @return the clock's time
     */
    public int getTime() {
        return this.time;
    }

    /**
     * Increments the logical clock.
     */
    public void tick() {
        this.time++;
    }

    /**
     * Compares this logical clock's time against a provided timestamp,
     * sets this clock's time to the greater of the two, and finally
     * increments this clock's time before returning the new time.
     * @param timestamp the time to compare against this clock
     * @return the clock's new time.
     */
    public int compareSetAndTick(int timestamp) {
        this.time = (this.time > (timestamp + 1)) ? this.time : timestamp + 1;
        this.tick();
        return this.time;
    }

}
