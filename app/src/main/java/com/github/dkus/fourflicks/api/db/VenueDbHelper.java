package com.github.dkus.fourflicks.api.db;


public class VenueDbHelper implements DbHelper.OnTableDdlListener {

    //Database table Venue
    public static final String TABLE_VENUE = "venue";
    public static final String VENUE_ID = "id";
    public static final String VENUE_NAME = "name";
    public static final String VENUE_ADDRESS = "addres";
    public static final String VENUE_LAT = "lat";
    public static final String VENUE_LNG = "lng";

    //Database table Venue DDL
    public static final String CREATE_TABLE_VENUE =
            "CREATE TABLE "
                    + TABLE_VENUE + "(" + VENUE_ID + " TEXT PRIMARY KEY, "
                    + VENUE_NAME + " TEXT, "
                    + VENUE_ADDRESS + " TEXT, "
                    + VENUE_LAT + " NUMERIC ,"
                    + VENUE_LNG + " NUMERIC " + ")";

    public static final String[] PROJECTION_ALL = {
            VENUE_ID,
            VENUE_NAME,
            VENUE_ADDRESS,
            VENUE_LAT,
            VENUE_LNG
    };

    public static final String SELECTION_BY_ID = VENUE_ID + " = ?";

    @Override
    public String getTableName() {
        return TABLE_VENUE;
    }

    @Override
    public String createTableDdl() {
        return CREATE_TABLE_VENUE;
    }

}
