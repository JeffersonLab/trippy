package org.jlab.trippy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TreeSet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jlab.mya.DataNexus;
import org.jlab.mya.Deployment;
import org.jlab.mya.Metadata;
import org.jlab.mya.event.IntEvent;
import org.jlab.mya.nexus.OnDemandNexus;
import org.jlab.mya.params.IntervalQueryParams;
import org.jlab.mya.service.IntervalService;
import org.jlab.mya.stream.IntEventStream;

/**
 *
 * @author ryans
 */
public class Restore {

    public static final int MAX_RECOVERY_SECONDS = 3600; // 1 Hour
    public static final String MASTER_FSD_NODE_PV = "ISD0I011G";
    public static final String HALL_A_RECOVERY_PV = "HLA:bta_bm_present";
    public static final String HALL_B_RECOVERY_PV = "HLB:bta_bm_present";
    public static final String HALL_C_RECOVERY_PV = "HLC:bta_bm_present";
    public static final String HALL_D_RECOVERY_PV = "HLD:bta_bm_present";
    public static final DateTimeFormatter TIMESTAMP_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    public void exportRecoveryToExcel(Instant begin, Instant end, String filepath) throws SQLException, IOException {
        DataNexus nexus = new OnDemandNexus(Deployment.ops);

        IntervalService service = new IntervalService(nexus);

        TripAndClearUpdates fsdUpdates = getFsdTripAndClearUpdates(service, MASTER_FSD_NODE_PV, begin, end);
        TreeSet<Instant> masterTripSet = fsdUpdates.tripSet;        
        TreeSet<Instant> masterRecoverySet = fsdUpdates.clearSet;

        TreeSet<Instant> hallARecoverySet = getIntUpdatesWithValue(service, HALL_A_RECOVERY_PV, begin, end, 1);
        TreeSet<Instant> hallBRecoverySet = getIntUpdatesWithValue(service, HALL_B_RECOVERY_PV, begin, end, 1);
        TreeSet<Instant> hallCRecoverySet = getIntUpdatesWithValue(service, HALL_C_RECOVERY_PV, begin, end, 1);
        TreeSet<Instant> hallDRecoverySet = getIntUpdatesWithValue(service, HALL_D_RECOVERY_PV, begin, end, 1);

        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("Trip Recovery");

        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("yyyy-MMM-dd HH:mm:ss"));

        int rowCounter = 0;
        Row row1 = sheet1.createRow(rowCounter++);
        row1.createCell(0).setCellValue("TRIP DOWN");
        row1.createCell(1).setCellValue("TRIP CLEAR");
        row1.createCell(2).setCellValue("HALL A RECOVERY");
        row1.createCell(3).setCellValue("HALL B RECOVERY");
        row1.createCell(4).setCellValue("HALL C RECOVERY");
        row1.createCell(5).setCellValue("HALL D RECOVERY");
        row1.createCell(6).setCellValue("TRIP SECONDS");
        row1.createCell(7).setCellValue("A RECOVERY SECONDS");
        row1.createCell(8).setCellValue("B RECOVERY SECONDS");
        row1.createCell(9).setCellValue("C RECOVERY SECONDS");
        row1.createCell(10).setCellValue("D RECOVERY SECONDS");

        for (Instant tripClear : masterRecoverySet) {

            Instant tripDown = masterTripSet.lower(tripClear);
            Instant nextTrip = masterTripSet.higher(tripClear);

            Instant hallARecoveryEnd = hallARecoverySet.higher(tripClear);
            Instant hallBRecoveryEnd = hallBRecoverySet.higher(tripClear);
            Instant hallCRecoveryEnd = hallCRecoverySet.higher(tripClear);
            Instant hallDRecoveryEnd = hallDRecoverySet.higher(tripClear);

            if (nextTrip != null) {
                if (hallARecoveryEnd != null && nextTrip.getEpochSecond() < hallARecoveryEnd.getEpochSecond()) {
                    hallARecoveryEnd = null;
                }

                if (hallBRecoveryEnd != null && nextTrip.getEpochSecond() < hallBRecoveryEnd.getEpochSecond()) {
                    hallBRecoveryEnd = null;
                }

                if (hallCRecoveryEnd != null && nextTrip.getEpochSecond() < hallCRecoveryEnd.getEpochSecond()) {
                    hallCRecoveryEnd = null;
                }

                if (hallDRecoveryEnd != null && nextTrip.getEpochSecond() < hallDRecoveryEnd.getEpochSecond()) {
                    hallDRecoveryEnd = null;
                }
            }

            Duration hallARecovery = hallARecoveryEnd == null ? null : Duration.between(tripClear, hallARecoveryEnd);
            Duration hallBRecovery = hallBRecoveryEnd == null ? null : Duration.between(tripClear, hallBRecoveryEnd);
            Duration hallCRecovery = hallCRecoveryEnd == null ? null : Duration.between(tripClear, hallCRecoveryEnd);
            Duration hallDRecovery = hallDRecoveryEnd == null ? null : Duration.between(tripClear, hallDRecoveryEnd);

            Duration tripRecovery = null;

            if (tripDown != null && tripClear != null) {
                tripRecovery = Duration.between(tripDown, tripClear);
            }

            if (tripRecovery != null && tripRecovery.getSeconds() < MAX_RECOVERY_SECONDS) {
                Row row = sheet1.createRow(rowCounter++);

                Cell c;

                if (tripDown != null) {
                    c = row.createCell(0);
                    c.setCellValue(Date.from(tripDown));
                    c.setCellStyle(dateStyle);
                }

                c = row.createCell(1);
                c.setCellValue(Date.from(tripClear));
                c.setCellStyle(dateStyle);

                row.createCell(6).setCellValue(tripRecovery.getSeconds());

                //System.out.println(formatter.format(tripClear) + " - ");
                if (hallARecovery != null && hallARecovery.getSeconds() < MAX_RECOVERY_SECONDS) {
                    c = row.createCell(2);
                    c.setCellValue(Date.from(hallARecoveryEnd));
                    c.setCellStyle(dateStyle);
                    row.createCell(7).setCellValue(hallARecovery.getSeconds());
                }

                if (hallBRecovery != null && hallBRecovery.getSeconds() < MAX_RECOVERY_SECONDS) {
                    c = row.createCell(3);
                    c.setCellValue(Date.from(hallBRecoveryEnd));
                    c.setCellStyle(dateStyle);
                    row.createCell(8).setCellValue(hallBRecovery.getSeconds());
                }

                if (hallCRecovery != null && hallCRecovery.getSeconds() < MAX_RECOVERY_SECONDS) {
                    c = row.createCell(4);
                    c.setCellValue(Date.from(hallCRecoveryEnd));
                    c.setCellStyle(dateStyle);
                    row.createCell(9).setCellValue(hallCRecovery.getSeconds());
                }

                if (hallDRecovery != null && hallDRecovery.getSeconds() < MAX_RECOVERY_SECONDS) {
                    c = row.createCell(5);
                    c.setCellValue(Date.from(hallDRecoveryEnd));
                    c.setCellStyle(dateStyle);
                    row.createCell(10).setCellValue(hallDRecovery.getSeconds());
                }
            }
        }

        // width is number of chars X 256
        sheet1.setColumnWidth(0, 20 * 256);
        sheet1.setColumnWidth(1, 20 * 256);
        sheet1.setColumnWidth(2, 20 * 256);
        sheet1.setColumnWidth(3, 20 * 256);
        sheet1.setColumnWidth(4, 20 * 256);
        sheet1.setColumnWidth(5, 20 * 256);
        sheet1.setColumnWidth(6, 20 * 256);
        sheet1.setColumnWidth(7, 20 * 256);
        sheet1.setColumnWidth(8, 20 * 256);
        sheet1.setColumnWidth(9, 20 * 256);
        sheet1.setColumnWidth(10, 20 * 256);

        try (FileOutputStream out = new FileOutputStream(filepath)) {
            wb.write(out);
        } // TODO: probably should use streaming API of POI
    }

    /**
     * @param args the command line arguments
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws SQLException, IOException {
        
        if(args.length != 3) {
            throw new RuntimeException("Usage: java org.jlab.Trippy start end filename");
        }
        
        String start = args[0];
        String finish = args[1];
        String filepath = args[2];     
        
        Instant begin = LocalDateTime.parse(start).atZone(ZoneId.systemDefault()).toInstant();
        Instant end = LocalDateTime.parse(finish).atZone(ZoneId.systemDefault()).toInstant();
        
        Restore trippy = new Restore();
        
        trippy.exportRecoveryToExcel(begin, end, filepath);
    }

    private TreeSet<Instant> getIntUpdatesWithValue(final IntervalService service, final String pv, final Instant begin, final Instant end, final int value) throws SQLException, IOException {
        Metadata metadata = service.findMetadata(pv);
        IntervalQueryParams params = new IntervalQueryParams(metadata, begin, end);

        TreeSet<Instant> set = new TreeSet<>();

        try (IntEventStream stream = service.openIntStream(params)) {

            IntEvent event;

            while ((event = stream.read()) != null) {

                if (event.getValue() == value) {
                    set.add(event.getTimestampAsInstant());
                }
            }
        }

        return set;
    }

    private TripAndClearUpdates getFsdTripAndClearUpdates(IntervalService service, String pv, Instant begin, Instant end) throws SQLException, IOException {
        Metadata metadata = service.findMetadata(pv);
        IntervalQueryParams params = new IntervalQueryParams(metadata, begin, end);

        TripAndClearUpdates fsdUpdates = new TripAndClearUpdates();

        // 0 and 1 values do not alternate as it is possible for a trip to occur during another trip as a trip just means a new non-zero value
        // However, we only care about first value change that started trip so we track last update and skip trips-in-a-trip
        boolean inTrip = false;

        try (IntEventStream stream = service.openIntStream(params)) {

            IntEvent event;

            while ((event = stream.read()) != null) {

                if (event.getValue() > 0) { // Not actually 1, and usuaully is just non zero integer
                    if (!inTrip) { // Skip a trip-in-a-trip
                        fsdUpdates.tripSet.add(event.getTimestampAsInstant());
                        inTrip = true;
                    }
                } else {
                    fsdUpdates.clearSet.add(event.getTimestampAsInstant());
                    inTrip = false;
                }
            }
        }

        return fsdUpdates;
    }
    
    private class TripAndClearUpdates {
        public TreeSet<Instant> tripSet = new TreeSet<>();
        public TreeSet<Instant> clearSet = new TreeSet<>();
    }
}
