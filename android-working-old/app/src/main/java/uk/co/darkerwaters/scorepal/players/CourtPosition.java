package uk.co.darkerwaters.scorepal.players;

public enum CourtPosition {
    /*for now we are doing north and south, but technically we could have more?*/
    NORTH(),
    SOUTH();

    public static final CourtPosition DEFAULT = NORTH;

    CourtPosition() {

    }

    public static String toString(CourtPosition position) {
        switch (position) {
            case NORTH:
                return "north";
            case SOUTH:
                return "south";
            default:
                return "none";
        }
    }

    public static CourtPosition fromString(String string) {
        switch (string) {
            case "north" :
                return NORTH;
            case "south" :
                return SOUTH;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return CourtPosition.toString(this);
    }

    public CourtPosition getNext() {
        int index = this.ordinal() + 1;
        if (index >= CourtPosition.values().length) {
            index = 0;
        }
        return CourtPosition.values()[index];
    }

}
