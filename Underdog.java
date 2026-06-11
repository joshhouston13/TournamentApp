public class Underdog extends Team {
    private double clutchFactor;
    //Allows a team to become an underdog with a clutch factor that activates 30% of the time.
    public Underdog(String teamName, int seed, double clutchFactor) {
        super(teamName, seed);
        this.clutchFactor = clutchFactor;
    }

    @Override
    public double matchPower() {
        double baseline = super.matchPower();
        if(Math.random() < 0.3) {
            return baseline + (this.clutchFactor * 4.0);
        }
        return baseline;
    }
}
